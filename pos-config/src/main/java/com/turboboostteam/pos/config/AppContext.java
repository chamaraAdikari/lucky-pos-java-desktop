package com.turboboostteam.pos.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AppContext {
    private static AnnotationConfigApplicationContext context;

    public static void init() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static void close() {
        if (context != null) context.close();
    }
}