package com.digitalascent.common.io;

import com.google.common.io.ByteSink;

import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GzipByteSink extends ByteSink {
    private final ByteSink delegate;
    private final int compressionLevel;

    public GzipByteSink(ByteSink delegate, int compressionLevel ) {
        this.delegate = checkNotNull(delegate, "delegate is required");
        this.compressionLevel = compressionLevel;
    }

    public GzipByteSink(ByteSink delegate ) {
        this( delegate, 6 );
    }

    @Override
    public OutputStream openStream() throws IOException {
        return new ConfigurableGzipOutputStream( delegate.openBufferedStream(), compressionLevel );
    }
}
