package com.digitalascent.common.instrumentation;

import com.digitalascent.common.base.StaticUtilityClass;
import com.google.common.base.Throwables;
import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

public class PlatformInstrumentation {

    /**
     * Executes the supplied Callable currently tracking thread memory allocation.
     *
     * @param callable Code to track memory allocations for
     * @param <T> Type of return value
     * @return
     */
    public <T> PlatformInstrumentationResult<T> executeWithInstrumentation(Callable<T> callable) {
        try {
            Thread currentThread = Thread.currentThread();
            ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
            long startMemory = threadMXBean.getThreadAllocatedBytes(currentThread.getId());

            T returnValue = callable.call();

            long allocatedMemory = threadMXBean.getThreadAllocatedBytes(currentThread.getId()) - startMemory;

            return new PlatformInstrumentationResult<>(returnValue, allocatedMemory);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    private PlatformInstrumentation() {
        StaticUtilityClass.throwCannotInstantiateError(getClass());
    }
}
