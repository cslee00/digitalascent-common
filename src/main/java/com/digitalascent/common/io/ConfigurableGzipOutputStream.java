package com.digitalascent.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public final class ConfigurableGzipOutputStream extends GZIPOutputStream {
    public ConfigurableGzipOutputStream(OutputStream out, int size, int compressionLevel ) throws IOException {
        super(out, size);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGzipOutputStream(OutputStream out, int size, boolean syncFlush, int compressionLevel ) throws IOException {
        super(out, size, syncFlush);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGzipOutputStream(OutputStream out, int compressionLevel ) throws IOException {
        super(out);
        setCompressionLevel( compressionLevel );
    }

    public ConfigurableGzipOutputStream(OutputStream out, boolean syncFlush, int compressionLevel ) throws IOException {
        super(out, syncFlush);
        setCompressionLevel( compressionLevel );
    }

    private void setCompressionLevel(int compressionLevel) {
        checkArgument(compressionLevel >= 1 && compressionLevel <=9, "compressionLevel >= 1 && compressionLevel <=9 : %s",compressionLevel );
        def.setLevel(compressionLevel);
    }
}
