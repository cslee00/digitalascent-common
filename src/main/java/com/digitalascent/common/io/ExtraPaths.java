package com.digitalascent.common.io;

import com.digitalascent.common.base.StaticUtilityClass;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.isDirectory;

public final class ExtraPaths {
    /**
     * Makes a gzip compressed copy of the provided file (adding .gz extension), deleting the original
     *
     * @param file
     */
    public static void compressFile(Path file) throws IOException {
        checkArgument(!isDirectory(file), "Files.isDirectory(file) : %s",  file);

        Path compressedFile = file.resolveSibling(file.toString() + ".gz");
        try (OutputStream outputStream = ExtraByteStreams.gzipOutputStream(new BufferedOutputStream(Files.newOutputStream(compressedFile)), GzipCompressionLevel.BEST_COMPRESSION )) {
            Files.copy(file, outputStream);
            Files.delete( file );
        }
    }

    private ExtraPaths() {

        StaticUtilityClass.throwCannotInstantiateError(getClass());
    }
}
