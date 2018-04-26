package com.digitalascent.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public final class ConfigurableGZIPOutputStream extends GZIPOutputStream {
    public ConfigurableGZIPOutputStream(OutputStream out, int size, GzipCompressionLevel compressionLevel ) throws IOException {
        super(out, size);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGZIPOutputStream(OutputStream out, int size, boolean syncFlush, GzipCompressionLevel compressionLevel ) throws IOException {
        super(out, size, syncFlush);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGZIPOutputStream(OutputStream out, GzipCompressionLevel compressionLevel ) throws IOException {
        super(out);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGZIPOutputStream(OutputStream out, boolean syncFlush, GzipCompressionLevel compressionLevel ) throws IOException {
        super(out, syncFlush);
        setCompressionLevel( compressionLevel );
    }

    private void setCompressionLevel(GzipCompressionLevel compressionLevel) {
        def.setLevel(compressionLevel.getValue());
    }
}
