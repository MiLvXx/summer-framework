package com.xuxin.summer.context;

import java.util.Objects;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * ApplicationContextUtils
 */
public class ApplicationContextUtils {

    private static ApplicationContext applicationContext;

    @Nonnull
    public static ApplicationContext getRequiredApplicationContext() {
        return Objects.requireNonNull(getApplicationContext(), "ApplicationContext is not set.");
    }

    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    static void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
    }
}