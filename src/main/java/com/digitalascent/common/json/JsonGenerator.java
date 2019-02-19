package com.digitalascent.common.json;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static com.digitalascent.common.json.JsonScope.DANGLING_NAME;
import static com.digitalascent.common.json.JsonScope.EMPTY_ARRAY;
import static com.digitalascent.common.json.JsonScope.EMPTY_DOCUMENT;
import static com.digitalascent.common.json.JsonScope.NONEMPTY_ARRAY;
import static com.digitalascent.common.json.JsonScope.NONEMPTY_DOCUMENT;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "ResultOfMethodCallIgnored"})
public final class JsonGenerator implements Closeable, Flushable {

    private static final char DOUBLE_QUOTE = '\"';
    private static final ImmutableSet<String> INVALID_NUMBER_VALUES = ImmutableSet.of("-Infinity", "Infinity", "NaN");
    private final Writer writer;
    private final SizeLimitedWriter encodingWriter;
    private final boolean serializeNulls;
    private final String sizeLimitReachedText;

    @Nullable
    private CharSequence deferredName;
    private boolean deferredNameIsJsonSafe;
    private JsonScope[] stack = new JsonScope[16];
    private int stackSize;

    public JsonGenerator(Writer writer, boolean serializeNulls, String sizeLimitReachedText) {
        this.writer = requireNonNull(writer, "writer");
        this.encodingWriter = new SizeLimitedWriter(new JsonEncodingWriter(writer));
        this.serializeNulls = serializeNulls;
        this.sizeLimitReachedText = sizeLimitReachedText;
        push(EMPTY_DOCUMENT);
    }

    public JsonGenerator beginArray() throws IOException {
        writeDeferredName();
        return open(EMPTY_ARRAY, "[");
    }

    public JsonGenerator endArray() throws IOException {
        return close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
    }

    public JsonGenerator beginObject() throws IOException {
        writeDeferredName();
        return open(JsonScope.EMPTY_OBJECT, "{");
    }

    public JsonGenerator endObject() throws IOException {
        return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}");
    }

    private void writeDeferredName() throws IOException {
        if (deferredName != null) {
            beforeName();
            string(deferredName, deferredNameIsJsonSafe ? writer : encodingWriter);
            resetDeferredName();
        }
    }

    private void resetDeferredName() {
        deferredName = null;
        deferredNameIsJsonSafe = false;
    }

    public JsonGenerator withValueWriter(ValueWriter valueWriter) throws IOException {
        return withValueWriter(valueWriter, Integer.MAX_VALUE);
    }

    public JsonGenerator withValueWriter(ValueWriter valueWriter, int sizeLimit) throws IOException {
        writeDeferredName();
        beforeValue();
        writer.write(DOUBLE_QUOTE);
        try {
            encodingWriter.resetSizeLimit(sizeLimit);
            valueWriter.writeWith(encodingWriter);
            encodingWriter.flush();
        } catch (SizeLimitedWriter.SizeLimitReachedException e) {
            if (sizeLimitReachedText != null) {
                encodingWriter.append(sizeLimitReachedText);
            }
        } finally {
            encodingWriter.flush();
            writer.write(DOUBLE_QUOTE);
            encodingWriter.resetSizeLimit(Integer.MAX_VALUE);
        }
        return this;
    }

    public JsonGenerator name(CharSequence name) {
        if (deferredName != null) {
            throw new IllegalStateException();
        }
        if (stackSize == 0) {
            throw new IllegalStateException("JsonWriter is closed.");
        }
        deferredName = name;
        deferredNameIsJsonSafe = false;
        return this;
    }

    public JsonGenerator safeName(CharSequence value) {
        deferredNameIsJsonSafe = true;
        return name(value);
    }

    private void beforeName() throws IOException {
        JsonScope context = peek();
        if (inNonEmptyObject(context)) {
            // first in object
            writer.write(',');
        } else if (!inEmptyObject(context)) {
            // not in an object!
            throw new IllegalStateException("Nesting problem");
        }
        replaceTop(DANGLING_NAME);
    }

    private static boolean inEmptyObject(JsonScope context) {
        return context == JsonScope.EMPTY_OBJECT;
    }

    private static boolean inNonEmptyObject(JsonScope context) {
        return context == JsonScope.NONEMPTY_OBJECT;
    }

    public JsonGenerator value(boolean value) throws IOException {
        writeDeferredName();
        beforeValue();
        writer.write(value ? "true" : "false");
        return this;
    }

    public JsonGenerator nullValue() throws IOException {
        if (deferredName != null) {
            if (serializeNulls) {
                writeDeferredName();
            } else {
                // skip the name and the value
                resetDeferredName();
                return this;
            }
        }
        beforeValue();
        writer.write("null");
        return this;
    }

    public JsonGenerator value(@Nullable Boolean value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        writer.write(value ? "true" : "false");
        return this;
    }

    public JsonGenerator value(double value) throws IOException {
        if ((Double.isNaN(value) || Double.isInfinite(value))) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        writeDeferredName();
        beforeValue();
        writer.append(Double.toString(value));
        return this;
    }

    public JsonGenerator value(long value) throws IOException {
        writeDeferredName();
        beforeValue();
        writer.write(Long.toString(value));
        return this;
    }

    public JsonGenerator value(@Nullable Number value) throws IOException {
        if (value == null) {
            return nullValue();
        }

        writeDeferredName();
        String string = value.toString();
        if (INVALID_NUMBER_VALUES.contains(string)) {
            throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
        }
        beforeValue();
        writer.append(string);
        return this;
    }

    public JsonGenerator value(@Nullable CharSequence value) throws IOException {
        if (value == null) {
            return nullValue();
        }
        writeDeferredName();
        beforeValue();
        string(value, encodingWriter);
        return this;
    }

    private void string(CharSequence value, Writer w) throws IOException {
        writer.write(DOUBLE_QUOTE);
        // writer may be the raw (unencoded) writer or the json-encoding writer
        w.append(value).flush();
        writer.write(DOUBLE_QUOTE);
    }

    private void beforeValue() throws IOException {
        switch (peek()) {
            case NONEMPTY_DOCUMENT:
                throw new IllegalStateException("JSON must have only one top-level value");

            case EMPTY_DOCUMENT:
                replaceTop(NONEMPTY_DOCUMENT);
                break;

            case EMPTY_ARRAY:
                replaceTop(NONEMPTY_ARRAY);
                break;

            case NONEMPTY_ARRAY:
                writer.append(',');
                break;

            case DANGLING_NAME:
                writer.append(':');
                replaceTop(JsonScope.NONEMPTY_OBJECT);
                break;

            default:
                throw new IllegalStateException("Nesting problem.");
        }
    }

    private void replaceTop(JsonScope topOfStack) {
        stack[stackSize - 1] = topOfStack;
    }

    public JsonGenerator safeValue(@Nullable CharSequence value) throws IOException {
        if (value == null) {
            return nullValue();
        }

        writeDeferredName();
        beforeValue();
        writer.write(DOUBLE_QUOTE);
        writer.append(value);
        writer.write(DOUBLE_QUOTE);
        return this;
    }

    @Override
    public void close() throws IOException {
        flush();
        encodingWriter.close();
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        if (stackSize == 0) {
            throw new IllegalStateException("JsonGenerator is closed.");
        }
        encodingWriter.flush();
        writer.flush();
    }

    private void push(JsonScope newTop) {
        if (stackSize == stack.length) {
            stack = Arrays.copyOf(stack, stackSize * 2);
        }
        stack[stackSize] = newTop;
        stackSize++;
    }

    private JsonGenerator open(JsonScope empty, String openChar) throws IOException {
        beforeValue();
        push(empty);
        writer.write(openChar);
        return this;
    }

    private JsonGenerator close(JsonScope empty, JsonScope nonempty, String closeChar) throws IOException {
        JsonScope context = peek();
        if (context != nonempty && context != empty) {
            throw new IllegalStateException("Nesting problem.");
        }
        if (deferredName != null) {
            throw new IllegalStateException("Dangling name: " + deferredName);
        }

        stackSize--;
        writer.write(closeChar);
        return this;
    }

    private JsonScope peek() {
        if (stackSize == 0) {
            throw new IllegalStateException("JsonGenerator is closed.");
        }
        return stack[stackSize - 1];
    }

    public interface ValueWriter {
        void writeWith(Writer writer) throws IOException;
    }
}
