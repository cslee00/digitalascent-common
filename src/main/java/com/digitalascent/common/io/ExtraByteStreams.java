package com.digitalascent.common.io;

import com.digitalascent.common.base.StaticUtilityClass;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ExtraByteStreams {

    public OutputStream suppressClose(OutputStream outputStream) {
        checkNotNull(outputStream, "outputStream is required");
        return new FilterOutputStream( outputStream ) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
    }

    public InputStream suppressClose(InputStream inputStream) {
        checkNotNull(inputStream, "inputStream is required");
        return new FilterInputStream(inputStream) {
            @Override
            public void close() throws IOException {
                // ignore close
            }
        };
    }

    private ExtraByteStreams() {
        StaticUtilityClass.throwCannotInstantiateError( getClass() );
    }
}
