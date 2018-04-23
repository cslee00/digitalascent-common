package com.digitalascent.common.base;

public final class StaticUtilityClass {

    public static void cannotInstantiate( Class<?> clazz ) {
        throw new AssertionError("Cannot instantiate static utility class " + clazz );
    }

    private StaticUtilityClass() {
        cannotInstantiate( getClass() );
    }
}
