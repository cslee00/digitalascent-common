package com.digitalascent.common.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JsonEncodingWriterTest {

    private StringWriter stringWriter;
    private JsonEncodingWriter jsonEncodingWriter;
    @BeforeEach
    void setup() {
        stringWriter = new StringWriter();
        jsonEncodingWriter = new JsonEncodingWriter(stringWriter);
    }

    private static Stream<Arguments> encodesSpecialCharactersParameters() {
        return Stream.of(
                Arguments.of("aaa", "aaa"),
                Arguments.of("\n", "\\n"),
                Arguments.of("\r", "\\r"),
                Arguments.of("\t", "\\t"),
                Arguments.of("\b", "\\b"),
                Arguments.of("\f", "\\f"),
                Arguments.of("\\", "\\\\"),
                Arguments.of("\u0000", "\\u0000"),
                Arguments.of("foo \"bar\"", "foo \\\"bar\\\"")
        );
    }

    @ParameterizedTest
    @MethodSource("encodesSpecialCharactersParameters")
    @SuppressWarnings("CheckReturnValue")
    void encodesSpecialCharacters(String input, String expectedOutput) throws IOException {
        jsonEncodingWriter.append(input);
        assertThat( stringWriter.toString() ).isEqualTo(expectedOutput);
    }

    @Test
    @DisplayName("encodes via write(char[])")
    void encodesViaWriteCharArray() throws IOException {
        jsonEncodingWriter.write("foo \"bar\"".toCharArray());
        assertThat( stringWriter.toString() ).isEqualTo("foo \\\"bar\\\"");
    }

    @Test
    @DisplayName("encodes via write(char[],int,int)")
    void encodesViaWriteCharArrayOffset() throws IOException {
        jsonEncodingWriter.write("foo \"bar\"".toCharArray(),3,6);
        assertThat( stringWriter.toString() ).isEqualTo(" \\\"bar\\\"");
    }

    @Test
    @DisplayName("encodes via write(String,int,int)")
    void encodesViaWriteStringOffset() throws IOException {
        jsonEncodingWriter.write("foo \"bar\"",3,6);
        assertThat( stringWriter.toString() ).isEqualTo(" \\\"bar\\\"");
    }

    @Test
    @DisplayName("encodes via append(CharSequence,int,int)")
    @SuppressWarnings("CheckReturnValue")
    void encodesViaAppendCharSequenceOffset() throws IOException {
        jsonEncodingWriter.append("foo \"bar\"",3,9);
        assertThat( stringWriter.toString() ).isEqualTo(" \\\"bar\\\"");
    }

    @Test
    @DisplayName("encodes via append(CharSequence)")
    @SuppressWarnings("CheckReturnValue")
    void encodesViaAppendCharSequence() throws IOException {
        jsonEncodingWriter.append("foo \"bar\"");
        assertThat( stringWriter.toString() ).isEqualTo("foo \\\"bar\\\"");
    }

    @Test
    @DisplayName("encodes via write(int)")
    @SuppressWarnings("CheckReturnValue")
    void encodesViaWriteChar() throws IOException {
        jsonEncodingWriter.write('\\');
        assertThat( stringWriter.toString() ).isEqualTo("\\\\");
    }

    @Test
    @DisplayName("encodes via append(char)")
    @SuppressWarnings("CheckReturnValue")
    void encodesViaAppendChar() throws IOException {
        jsonEncodingWriter.append('\\');
        assertThat( stringWriter.toString() ).isEqualTo("\\\\");
    }
}