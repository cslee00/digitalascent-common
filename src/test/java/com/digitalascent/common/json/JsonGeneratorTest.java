package com.digitalascent.common.json;


import com.digitalascent.common.io.CharArrayWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonGeneratorTest {

    private CharArrayWriter charArrayWriter;
    private JsonGenerator jsonGenerator;
    @BeforeEach
    void setup() {
        charArrayWriter = new CharArrayWriter(1);
        jsonGenerator = new JsonGenerator(charArrayWriter, true, "...");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesSimpleJson() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .value("bar")
                .name("field2")
                .value("value2")
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":\"bar\",\"field2\":\"value2\"}");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesEncodedJson() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .value("bar\n\r")
                .name("field2")
                .value("foo \"bar\"\n")
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":\"bar\\n\\r\",\"field2\":\"foo \\\"bar\\\"\\n\"}");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesEncodedJsonViaWriter() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .value("bar")
                .name("field2")
                .withValueWriter(encodingWriter -> {
                    encodingWriter.write("foo \"bar\"\n");
                })
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":\"bar\",\"field2\":\"foo \\\"bar\\\"\\n\"}");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesEncodedJsonViaWriterWithSizeLimit() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .value("bar")
                .name("field2")
                .withValueWriter(encodingWriter -> {
                    encodingWriter.write("foo \"bar\"\n");
                }, 5)
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":\"bar\",\"field2\":\"foo \\\"...\"}");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesNestedJson() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .beginObject()
                .name("field2")
                .value("value2")
                .endObject()
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":{\"field2\":\"value2\"}}");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    void generatesObjectArrayJson() throws IOException {
        jsonGenerator
                .beginObject()
                .name("foo")
                .beginArray()
                .value("value1")
                .value("value2")
                .value("value3")
                .endArray()
                .endObject();

        assertThat(charArrayWriter.toString()).isEqualTo("{\"foo\":[\"value1\",\"value2\",\"value3\"]}");
    }
}