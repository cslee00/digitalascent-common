package com.digitalascent.common.io;

public enum GzipCompressionLevel {
    NO_COMPRESSION(0), BEST_SPEED(1), BALANCED(6), BEST_COMPRESSION(9);

    private final int value;

    GzipCompressionLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
