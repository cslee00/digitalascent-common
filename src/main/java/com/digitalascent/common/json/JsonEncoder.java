package com.digitalascent.common.json;

import javax.annotation.Nullable;

final class JsonEncoder {
    // sparsely populated array of replacements; char values to be replaced are populated, others are null
    private static final String[] REPLACEMENT_CHARS = new String[128];

    static {
        // unicode-escape everything up to 0x1f
        for (int i = 0; i <= 0x1f; i++) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
        }

        // replace these with specific escape sequences
        REPLACEMENT_CHARS['"'] = "\\\"";
        REPLACEMENT_CHARS['\\'] = "\\\\";
        REPLACEMENT_CHARS['\t'] = "\\t";
        REPLACEMENT_CHARS['\b'] = "\\b";
        REPLACEMENT_CHARS['\n'] = "\\n";
        REPLACEMENT_CHARS['\r'] = "\\r";
        REPLACEMENT_CHARS['\f'] = "\\f";
    }

    @Nullable static String replacementFor(char c) {
        if (c < REPLACEMENT_CHARS.length) {
            // will return specific replacement (if any), otherwise null
            return REPLACEMENT_CHARS[c];
        }
        if (c == '\u2028') {
            return "\\u2028";
        }
        if (c == '\u2029') {
            return "\\u2029";
        }
        return null;
    }

    private JsonEncoder() {
        throw new AssertionError( "Cannot instantiate " + getClass() );
    }
}
