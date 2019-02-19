package com.digitalascent.common.json;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

final class SizeLimitedWriter extends FilterWriter {

    static class SizeLimitReachedException extends RuntimeException {
        private static final long serialVersionUID = 123456L;
    }

    private int sizeLimit = Integer.MAX_VALUE;
    private int size;

    SizeLimitedWriter(Writer out) {
        super(out);
    }

    private int calculateRemaining(int len) {
        if( len <= 0 ) {
            return 0;
        }
        int toWrite = Math.min(len, sizeLimit - size);
        return toWrite < 0 ? 0 : toWrite;
    }

    private void checkSizeLimit(int sizeToAdd) {
        if (size + sizeToAdd > sizeLimit) {
            throw new SizeLimitReachedException();
        }
        size += sizeToAdd;
    }

    void resetSizeLimit(int sizeLimit) {
        this.size = 0;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public void write(int c) throws IOException {
        int sizeToWrite = calculateRemaining(1);
        if (sizeToWrite > 0) {
            out.write(c);
        }
        checkSizeLimit(1);
    }

    @Override
    public void write(char[] buffer, int off, int len) throws IOException {
        int sizeToWrite = calculateRemaining(len);
        if (sizeToWrite > 0) {
            out.write(buffer, off, sizeToWrite);
        }
        checkSizeLimit(len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        int sizeToWrite = calculateRemaining(len);
        if (sizeToWrite > 0) {
            out.write(str, off, sizeToWrite);
        }
        checkSizeLimit(len);
    }

    @Override
    public void write(char[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return append(csq, 0, csq.length());
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        int length = end - start;
        int remaining = calculateRemaining(length);
        if( remaining < length ) {
            end = start + remaining;
        }
        if (end > start) {
            out.append(csq, start, end);
        }
        checkSizeLimit(length);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }
}
