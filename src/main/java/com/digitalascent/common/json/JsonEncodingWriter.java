package com.digitalascent.common.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ResultOfMethodCallIgnored")
final class JsonEncodingWriter extends Writer {
    private final Writer out;

    JsonEncodingWriter(Writer out) {
        this.out = requireNonNull(out, "out");
    }

    @Override
    public void write(char[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(String string) throws IOException {
        write(string, 0, string.length());
    }

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    @Override
    public void write(char[] buffer, int off, int len) throws IOException {
        writeEncoded(buffer, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        String replacement = JsonEncoder.replacementFor((char) c);
        if (replacement == null) {
            out.write(c);
        } else {
            out.write(replacement);
        }
    }

    @Override
    public void write(String string, int off, int len) throws IOException {
        append(string, off, off + len);
    }

    @Override
    public Writer append(CharSequence charSequence) throws IOException {
        return append(charSequence, 0, charSequence.length());
    }

    @Override
    public Writer append(CharSequence charSequence, int start, int end) throws IOException {
        writeEncoded(charSequence, start, end);
        return this;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private void writeEncoded(char[] buffer, int offset, int length) throws IOException {
        writeEncoded(CharBuffer.wrap(buffer, offset, length), 0, length);
    }

    private void writeEncoded(CharSequence charSequence, int offset, int end) throws IOException {
        if (end > charSequence.length()) {
            throw new IndexOutOfBoundsException(String.valueOf(end));
        }
        int last = offset;
        for (int i = offset; i < end; i++) {
            char c = charSequence.charAt(i);
            String replacement = JsonEncoder.replacementFor(c);
            if (replacement == null) {
                continue;
            }
            if (last < i) {
                // write out all previously scanned characters that didn't need replacement
                out.append(charSequence, last, i);
            }
            // write out replacement
            out.write(replacement);
            last = i + 1;
        }
        if (last < end) {
            out.append(charSequence, last, end);
        }
    }
}
