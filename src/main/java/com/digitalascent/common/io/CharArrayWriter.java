package com.digitalascent.common.io;

import java.io.Writer;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * High performance writer that directly targets a char[].
 *
 * Differs from java.io.CharArrayWriter:
 * removes synchronization
 * optimizes append( CharSequence ) where CharSequence is a String.
 * adds safeReset() method to zero out character array on reset
 */
@SuppressWarnings("unused")
public final class CharArrayWriter extends Writer {

    private char[] buffer;
    private int count;

    public CharArrayWriter(int initialSize) {
        checkArgument(initialSize > 0, "initialSize > 0 : %s", initialSize);
        buffer = new char[initialSize];
    }

    @Override
    public void write(char[] buffer, int offset, int length) {
        if (length == 0) {
            return;
        }

        if ((offset < 0) || (offset > buffer.length) || (length < 0) ||
                ((offset + length) > buffer.length) || ((offset + length) < 0)) {
            throw new IndexOutOfBoundsException();
        }

        int newCount = ensureCapacity(length);
        System.arraycopy(buffer, offset, this.buffer, count, length);
        count = newCount;
    }

    @Override
    public void write(int character) {
        int newCount = ensureCapacity(1);
        buffer[count] = (char) character;
        count = newCount;
    }

    @Override
    public void write(char[] buffer) {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(String string) {
        write(string, 0, string.length());
    }

    @Override
    public void write(String string, int offset, int length) {
        int newCount = ensureCapacity(length);
        string.getChars(offset, offset + length, buffer, count);
        count = newCount;
    }

    @Override
    public Writer append(CharSequence charSequence) {
        String s = String.valueOf(charSequence);
        write(s, 0, s.length());
        return this;
    }

    @Override
    public Writer append(CharSequence charSequence, int start, int end) {
        if (charSequence == null) {
            charSequence = "null";
        }
        // optimization - write string directly
        if (charSequence instanceof String) {
            write((String) charSequence, start, end - start);
            return this;
        }

        // not a string, use slow path
        return append(charSequence.subSequence(start, end));
    }

    @Override
    public Writer append(char character) {
        write(character);
        return this;
    }

    private int ensureCapacity(int length) {
        int newCount = count + length;
        if (newCount > buffer.length) {
            buffer = Arrays.copyOf(buffer, Math.max(buffer.length << 1, newCount));
        }
        return newCount;
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }

    public void reset() {
        count = 0;
    }

    public void safeReset() {
        // wipe out the used elements in the array
        Arrays.fill(buffer,0,count,(char)0);
        reset();
    }

    public int size() {
        return count;
    }

    @Override
    public String toString() {
        return new String(buffer, 0, count);
    }
}
