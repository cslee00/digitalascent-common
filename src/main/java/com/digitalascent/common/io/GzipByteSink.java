package com.digitalascent.common.io;

import com.google.common.io.ByteSink;
import com.google.errorprone.annotations.MustBeClosed;

import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GzipByteSink extends ByteSink {
    private final ByteSink delegate;
    private final GzipCompressionLevel compressionLevel;

    public GzipByteSink(ByteSink delegate, GzipCompressionLevel compressionLevel) {
        this.delegate = checkNotNull(delegate, "delegate is required");
        this.compressionLevel = checkNotNull(compressionLevel, "compressionLevel is required");
    }

    public GzipByteSink(ByteSink delegate) {
        this(delegate, GzipCompressionLevel.BALANCED);
    }

    @MustBeClosed
    @Override
    public OutputStream openStream() throws IOException {
        return new ConfigurableGZIPOutputStream(delegate.openBufferedStream(), compressionLevel);
    }
}
