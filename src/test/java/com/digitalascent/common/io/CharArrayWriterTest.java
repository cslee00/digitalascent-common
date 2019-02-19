package com.digitalascent.common.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharArrayWriterTest {

    private CharArrayWriter charArrayWriter;

    @BeforeEach
    void setup() {
        this.charArrayWriter = new CharArrayWriter(1);
    }

    @Test
    @DisplayName("writes via write(String,int,int)")
    void viaWriteStringOffset() {
        charArrayWriter.write("foo \"bar\"", 3, 6);
        assertThat( charArrayWriter.toString()).isEqualTo(" \"bar\"");
    }
    @Test
    @DisplayName("writes via write(String,int,int)")
    void viaWriteString() {
        charArrayWriter.write("foo \"bar\"");
        assertThat( charArrayWriter.toString()).isEqualTo("foo \"bar\"");
    }

    @Test
    @DisplayName("writes via write(char[])")
    void viaWriteCharArray() {
        charArrayWriter.write("foo \"bar\"".toCharArray());
        assertThat( charArrayWriter.toString()).isEqualTo("foo \"bar\"");
    }

    @Test
    @DisplayName("writes via write(char[],int,int)")
    void viaWriteCharArrayOffset() {
        charArrayWriter.write("foo \"bar\"".toCharArray(), 3, 6);
        assertThat( charArrayWriter.toString()).isEqualTo(" \"bar\"");
    }

    @Test
    @DisplayName("writes via write(char)")
    void viaWriteChar() {
        charArrayWriter.write('a');
        charArrayWriter.write('a');
        charArrayWriter.write('a');
        assertThat( charArrayWriter.toString()).isEqualTo("aaa");
    }

    @Test
    @DisplayName("writes via append(char)")
    @SuppressWarnings("CheckReturnValue")
    void viAppendChar() {
        charArrayWriter.append('a');
        charArrayWriter.append('a');
        charArrayWriter.append('a');
        assertThat( charArrayWriter.toString()).isEqualTo("aaa");
    }

    @Test
    @DisplayName("writes via append(CharSequence)")
    @SuppressWarnings("CheckReturnValue")
    void viAppendCharSequence() {
        charArrayWriter.append("foo bar baz");
        assertThat( charArrayWriter.toString()).isEqualTo("foo bar baz");
    }

    @Test
    @DisplayName("writes via append(CharSequence,int,int)")
    @SuppressWarnings("CheckReturnValue")
    void viAppendCharSequenceOffset() {
        charArrayWriter.append("foo bar baz",4,7);
        assertThat( charArrayWriter.toString()).isEqualTo("bar");
    }
}