package com.digitalascent.common.io;

import com.google.common.io.ByteSource;
import com.google.errorprone.annotations.MustBeClosed;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GzipByteSource extends ByteSource {
    private final ByteSource delegate;

    public GzipByteSource(ByteSource delegate ) {
        this.delegate = checkNotNull(delegate, "delegate is required");
    }

    @MustBeClosed
    @Override
    public InputStream openStream() throws IOException {
        return new GZIPInputStream( delegate.openBufferedStream() );
    }
}
