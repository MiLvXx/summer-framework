package com.xuxin.summer.io;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;

public class PropertyResolver {

    Logger logger = LoggerFactory.getLogger(getClass());
    Map<String, String> properties = new HashMap<>();
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public PropertyResolver(Properties properties) {
        // 存入环境变量
        this.properties.putAll(System.getenv());
        
        Set<String> names = properties.stringPropertyNames();
        for (String name : names) {
            this.properties.put(name, properties.getProperty(name));
        }
        if (logger.isDebugEnabled()) {
            List<String> keys = new ArrayList<>(this.properties.keySet());
            keys.sort(Comparator.naturalOrder());
            for (String key : keys) {
                logger.debug("PropertyResolver: {} = {}", key, this.properties.get(key));
            }
        }

        converters.put(String.class, s -> s);

        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Boolean.class, Boolean::valueOf);

        converters.put(byte.class, Byte::parseByte);
        converters.put(Byte.class, Byte::valueOf);

        converters.put(short.class, Short::parseShort);
        converters.put(Short.class, Short::valueOf);

        converters.put(int.class, Integer::parseInt);
        converters.put(Integer.class, Integer::valueOf);

        converters.put(long.class, Long::parseLong);
        converters.put(Long.class, Long::valueOf);

        converters.put(float.class, Float::parseFloat);
        converters.put(Float.class, Float::valueOf);

        converters.put(double.class, Double::parseDouble);
        converters.put(Double.class, Double::valueOf);

        converters.put(LocalDate.class, LocalDate::parse);
        converters.put(LocalTime.class, LocalTime::parse);
        converters.put(LocalDateTime.class, LocalDateTime::parse);
        converters.put(ZonedDateTime.class, ZonedDateTime::parse);
        converters.put(Duration.class, Duration::parse);
        converters.put(ZoneId.class, ZoneId::of);
    }

    @Nullable
    public String getProperty(String key) {
        // 解析${abc.xyz:defaultValue}
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr != null) {
            if (keyExpr.defaultValue() != null) {
                // 带默认值查询
                return getProperty(keyExpr.key(), keyExpr.defaultValue());
            } else {
                return getRequiredProperty(keyExpr.key());
            }
        }
        String value = this.properties.get(key);
        if (value != null) {
            return parseValue(value);
        }
        return null;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? parseValue(defaultValue) : value;
    }

    @Nullable
    public <T> T getProperty(String key, Class<T> targetType) {
        String value = getProperty(key);
        return value == null ? null : convert(targetType, value);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : convert(targetType, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Class<?> clazz, String value) {
        Function<String, Object> fn = this.converters.get(clazz);
        if (fn == null) {
            throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
        }
        return ((T) fn.apply(value));
    }

    private String parseValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getRequiredProperty(expr.key());
        }
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }
   
    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        T value = getProperty(key, targetType);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }

    PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            // 是否存在defaultValue?
            int n = key.indexOf(':');
            if (n == -1) {
                // 不存在：${key}
                String k = notEmpty(key.substring(2, key.length() - 1));
                return new PropertyExpr(k, null);
            } else {
                // 存在：${key:default}
                String k = notEmpty(key.substring(2, n));
                return new PropertyExpr(k, key.substring(n + 1, key.length() - 1));
            }
        }

        return null;
    }

    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return key;
    }
}
