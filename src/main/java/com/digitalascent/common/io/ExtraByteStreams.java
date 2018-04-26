package com.digitalascent.common.io;

import com.digitalascent.common.base.StaticUtilityClass;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ExtraByteStreams {

    public GZIPInputStream gzipInputStream(InputStream inputStream) throws IOException {
        checkNotNull(inputStream, "inputStream is required");
        return new GZIPInputStream(inputStream);
    }

    public GZIPOutputStream gzipOutputStream(OutputStream outputStream) throws IOException {
        return gzipOutputStream(outputStream, GzipCompressionLevel.BALANCED);
    }

    public GZIPOutputStream gzipOutputStream(OutputStream outputStream, GzipCompressionLevel compressionLevel) throws IOException {
        checkNotNull(outputStream, "outputStream is required");
        checkNotNull(compressionLevel, "compressionLevel is required");

        return new ConfigurableGZIPOutputStream(outputStream,compressionLevel);
    }

    public OutputStream closeSupressingOutputStream(OutputStream outputStream) {
        checkNotNull(outputStream, "outputStream is required");
        return new FilterOutputStream( outputStream ) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
    }

    public InputStream closeSupressingInputStream(InputStream inputStream) {
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
