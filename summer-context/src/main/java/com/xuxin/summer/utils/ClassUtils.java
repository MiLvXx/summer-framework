package com.xuxin.summer.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.exception.BeanDefinitionException;

import jakarta.annotation.Nullable;

public class ClassUtils {

    /*
     * 递归查找注解
     * 此方法并非简单在Class定义查看注解
     * 例如查找@Component，但是@Controller也符合要求
     */
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annoClass) {
        A a = target.getAnnotation(annoClass);
        for (Annotation anno : target.getAnnotations()) {
            Class<? extends Annotation> annoType = anno.annotationType();
            if (!annoType.getPackageName().equals("java.lang.annotation")) {
                A found = findAnnotation(annoType, annoClass);
                if (found != null) {
                    if (a != null) {
                        throw new BeanDefinitionException("Duplicate @" + annoClass.getSimpleName() + " found on class "
                                + target.getSimpleName());
                    }
                    a = found;
                }
            }
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <A extends Annotation> A getAnnotation(Annotation[] annotations, Class<A> annoClass) {
        for (Annotation annotation : annotations) {
            if (annoClass.isInstance(annotation)) {
                return ((A) annotation);
            }
        }
        return null;
    }

    public static String getBeanName(Method method) {
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        if (name.isEmpty()) {
            name = method.getName();
        }
        return name;
    }

    public static String getBeanName(Class<?> clazz) {
        String name = "";
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            name = component.value();
        } else {
            for (Annotation anno : clazz.getAnnotations()) {
                if (findAnnotation(anno.annotationType(), Component.class) != null) {
                    try {
                        name = (String) anno.annotationType().getMethod("value").invoke(anno);
                    } catch (ReflectiveOperationException e) {
                        throw new BeanDefinitionException("Cannot get annotation value.", e);
                    }
                }
            }
        }
        if (name.isEmpty()) {
            name = clazz.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static Method findAnnotationMethod(Class<?> clazz, Class<? extends Annotation> annoClass) {
        List<Method> ms = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annoClass))
                .peek(m -> {
                    if (m.getParameterCount() != 0) {
                        throw new BeanDefinitionException(
                                String.format("Method '%s' with @%s must not have argument: %s", m.getName(),
                                        annoClass.getSimpleName(), clazz.getName()));
                    }
                }).toList();
        if (ms.isEmpty()) {
            return null;
        }
        if (ms.size() == 1) {
            return ms.get(0);
        }
        throw new BeanDefinitionException(String.format("Multiple methods with @%s found in class: %s",
                annoClass.getSimpleName(), clazz.getName()));
    }

    public static Method getNamedMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (ReflectiveOperationException e) {
            throw new BeanDefinitionException(
                    String.format("Method '%s' not found in class: %s", methodName, clazz.getName()));
        }
    }
}
