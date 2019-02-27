package com.digitalascent.common.instrumentation;

import javax.annotation.Nullable;

public final class PlatformInstrumentationResult<T> {
    @Nullable
    private final T value;
    private final long allocatedBytes;

    PlatformInstrumentationResult(@Nullable T value, long allocatedBytes) {
        this.value = value;
        this.allocatedBytes = allocatedBytes;
    }

    @Nullable
    public T value() {
        return value;
    }

    public long allocatedBytes() {
        return allocatedBytes;
    }
}
