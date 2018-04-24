package com.digitalascent.common.base;

public final class StaticUtilityClass {

    private static class CannotInstantiateStaticUtilityClassError extends AssertionError {
        CannotInstantiateStaticUtilityClassError(Class<?> clazz) {
            super("Static utility class cannot be instantiated: " + clazz);
        }
    }

    public static void throwCannotInstantiateError(Class<?> clazz ) {
        throw new CannotInstantiateStaticUtilityClassError( clazz );
    }

    private StaticUtilityClass() {
        throwCannotInstantiateError( getClass() );
    }
}
