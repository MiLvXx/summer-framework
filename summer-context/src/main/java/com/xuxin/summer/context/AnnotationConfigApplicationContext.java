package com.xuxin.summer.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuxin.summer.annotation.Autowired;
import com.xuxin.summer.annotation.Bean;
import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.ComponentScan;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Import;
import com.xuxin.summer.annotation.Order;
import com.xuxin.summer.annotation.Primary;
import com.xuxin.summer.annotation.Value;
import com.xuxin.summer.exception.BeanCreationException;
import com.xuxin.summer.exception.BeanDefinitionException;
import com.xuxin.summer.exception.BeanNotOfRequiredTypeException;
import com.xuxin.summer.exception.NoSuchBeanDefinitionException;
import com.xuxin.summer.exception.NoUniqueBeanDefinitionException;
import com.xuxin.summer.exception.UnsatisfiedDependencyException;
import com.xuxin.summer.io.PropertyResolver;
import com.xuxin.summer.io.ResourceResolver;
import com.xuxin.summer.utils.ClassUtils;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class AnnotationConfigApplicationContext implements ConfigurableApplicationContext {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final PropertyResolver propertyResolver;
    protected final Map<String, BeanDefinition> beans;

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private final Set<String> creatingBeanNames;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) {
        ApplicationContextUtils.setApplicationContext(this);
        this.propertyResolver = propertyResolver;

        // 扫描获取所有Bean的Class类型
        final Set<String> beanClassNames = scanForClassNames(configClass);

        // 创建Bean的定义
        this.beans = createBeanDefinitions(beanClassNames);

        // 创建BeanName检测依赖循环
        this.creatingBeanNames = new HashSet<>();

        // 创建@Configuration类型的Bean
        // 此类Bean为工厂，应当首先实例化
        this.beans.values().stream()
                .filter(this::isConfigurationDefinition)
                .sorted()
                .forEach(this::createBeanAsEarlySingleton);
        // 创建BeanPostProcessor类型的Bean
        List<BeanPostProcessor> processors = this.beans.values().stream()
                .filter(this::isBeanPostProcessorDefinition)
                .sorted()
                .map(def -> (BeanPostProcessor) createBeanAsEarlySingleton(def)).toList();
        this.beanPostProcessors.addAll(processors);

        // 创建其他普通Bean
        createNormalBeans();

        // 通过字段和set方法注入依赖
        this.beans.values().forEach(this::injectBean);

        // 调用init方法
        this.beans.values().forEach(this::initBean);

        if (logger.isDebugEnabled()) {
            this.beans.values().stream().sorted().forEach(def -> logger.debug("bean initialized: {}", def));
        }
    }

    /**
     * 调用init方法
     *
     * @param def the definition of bean
     */
    void initBean(BeanDefinition def) {
        callMethod(def.getInstance(), def.getInitMethod(), def.getInitMethodName());
    }

    private void callMethod(Object instance, Method method, String namedMethod) {
        // 调用init/destroy方法
        if (method != null) {
            try {
                method.invoke(instance);
            } catch (ReflectiveOperationException e) {
                throw new BeanCreationException(e);
            }
        } else if (namedMethod != null) {
            Method named = ClassUtils.getNamedMethod(instance.getClass(), namedMethod);
            named.setAccessible(true);
            try {
                named.invoke(instance);
            } catch (ReflectiveOperationException e) {
                throw new BeanCreationException(e);
            }
        }
    }

    /**
     * 注入依赖但不调用init方法
     *
     * @param def the definition of bean
     */
    void injectBean(BeanDefinition def) {
        // 获取Bean实例，或被代理的原始实例
        final Object beanInstance = getProxiedInstance(def);
        try {
            injectProperties(def, def.getBeanClass(), beanInstance);
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(e);
        }
    }

    private Object getProxiedInstance(BeanDefinition def) {
        Object beanInstance = def.getInstance();
        List<BeanPostProcessor> reversedBeanPostProcessors = new ArrayList<>(this.beanPostProcessors);
        Collections.reverse(reversedBeanPostProcessors);
        for (BeanPostProcessor beanPostProcessor : reversedBeanPostProcessors) {
            Object restoredInstance = beanPostProcessor.postProcessOnSetProperty(beanInstance, def.getName());
            if (restoredInstance != beanInstance) {
                assert beanInstance != null;
                logger.atDebug().log("BeanPostProcessor {} specified injection from {} to {}.",
                        beanPostProcessor.getClass().getSimpleName(),
                        beanInstance.getClass().getSimpleName(), restoredInstance.getClass().getSimpleName());
                beanInstance = restoredInstance;
            }
        }
        return beanInstance;
    }

    /*
     * 注入属性
     */
    void injectProperties(BeanDefinition def, Class<?> beanClass, Object bean)
            throws ReflectiveOperationException {
        // 当前类
        for (Field field : beanClass.getDeclaredFields()) {
            tryInjectProperties(def, beanClass, bean, field);
        }
        for (Method method : beanClass.getDeclaredMethods()) {
            tryInjectProperties(def, beanClass, bean, method);
        }
        // 在父类查找Field和Method并注入
        Class<?> superclass = beanClass.getSuperclass();
        if (superclass != null) {
            injectProperties(def, superclass, bean);
        }
    }

    /*
     * 注入单个属性
     */
    void tryInjectProperties(BeanDefinition def, Class<?> clazz, Object bean, AccessibleObject acc)
            throws ReflectiveOperationException {
        Value value = acc.getAnnotation(Value.class);
        Autowired autowired = acc.getAnnotation(Autowired.class);
        if (value == null && autowired == null) {
            return;
        }

        Field field = null;
        Method method = null;
        if (acc instanceof Field f) {
            checkFieldOrMethod(f);
            f.setAccessible(true);
            field = f;
        }
        if (acc instanceof Method m) {
            checkFieldOrMethod(m);
            if (m.getParameters().length != 1) {
                throw new BeanDefinitionException(
                        String.format("Cannot inject a non-setter method %s for bean '%s': %s", m.getName(),
                                def.getName(), def.getBeanClass().getName()));
            }
            m.setAccessible(true);
            method = m;
        }

        String accessibleName;
        if (field != null) {
            accessibleName = field.getName();
        } else {
            assert method != null;
            accessibleName = method.getName();
        }
        Class<?> accessibleType = field != null ? field.getType() : method.getParameterTypes()[0];

        if (value != null && autowired != null) {
            throw new BeanCreationException(
                    String.format("Cannot specify both @Autowired and @Value when inject %s.%s for bean '%s': %s",
                            clazz.getSimpleName(), accessibleName, def.getName(), def.getBeanClass().getName()));
        }

        // @Value注入
        if (value != null) {
            Object propValue = this.propertyResolver.getRequiredProperty(value.value(), accessibleType);
            inject(def, bean, field, method, accessibleName, propValue);
        }

        // @Autowired
        if (autowired != null) {
            String name = autowired.name();
            boolean required = autowired.value();
            Object depends = name.isEmpty() ? findBean(accessibleType) : findBean(name, accessibleType);
            if (required && depends == null) {
                throw new UnsatisfiedDependencyException(
                        String.format("Dependency bean not found when inject %s.%s for bean '%s': %s",
                                clazz.getSimpleName(),
                                accessibleName, def.getName(), def.getBeanClass().getName()));
            }
            if (depends != null) {
                inject(def, bean, field, method, accessibleName, depends);
            }
        }
    }

    private void inject(BeanDefinition def, Object bean, Field field, Method method, String accessibleName, Object propValue) throws IllegalAccessException, InvocationTargetException {
        if (field != null) {
            logger.atDebug().log("Field injection: {}.{} = {}", def.getBeanClass().getName(), accessibleName,
                    propValue);
            field.set(bean, propValue);
        }
        if (method != null) {
            logger.atDebug().log("Method injection: {}.{} ({})", def.getBeanClass().getName(), accessibleName,
                    propValue);
            method.invoke(bean, propValue);
        }
    }

    void checkFieldOrMethod(Member member) {
        int mod = member.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new BeanDefinitionException("Cannot inject static field: " + member);
        }
        if (Modifier.isFinal(mod)) {
            if (member instanceof Field field) {
                throw new BeanDefinitionException("Cannot inject final field: " + field);
            }
            if (member instanceof Method) {
                logger.warn(
                        "Inject final method should be careful because it is not called on target bean when bean is proxied and may cause NullPointerException.");
            }
        }
    }

    /**
     * 创建普通Bean
     */
    void createNormalBeans() {
        // 过滤
        List<BeanDefinition> defs = this.beans.values().stream()
                .filter(def -> def.getInstance() == null)
                .sorted().toList();
        // 依次创建
        defs.forEach(def -> {
            // 如果Bean未被创建(可能在其他Bean的构造方法注入前被创建):
            if (def.getInstance() == null) {
                createBeanAsEarlySingleton(def);
            }
        });
    }

    /**
     * 创建一个Bean，但不进行字段和方法级别的注入。
     * 如果创建的Bean不是Configuration, 则在构造方法中注入的依赖Bean会自动创建
     */
    public Object createBeanAsEarlySingleton(BeanDefinition def) {
        logger.atDebug().log("Try create bean '{}' as early singleton: {}", def.getName(),
                def.getBeanClass().getName());
        // 检测依赖循环
        if (!this.creatingBeanNames.add(def.getName())) {
            throw new UnsatisfiedDependencyException(
                    String.format("Circular dependency detected when create bean '%s'", def.getName()));
        }

        // 创建方式：构造方法或工厂方法
        Executable createFn;
        if (def.getFactoryName() == null) {
            // by constructor
            createFn = def.getConstructor();
        } else {
            // by factory method
            createFn = def.getFactoryMethod();
        }

        assert createFn != null;
        final Parameter[] parameters = createFn.getParameters();
        final Annotation[][] parameterAnnotations = createFn.getParameterAnnotations();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Parameter param = parameters[i];
            final Annotation[] paramAnnotations = parameterAnnotations[i];
            final Value value = ClassUtils.getAnnotation(paramAnnotations, Value.class);
            final Autowired autowired = ClassUtils.getAnnotation(paramAnnotations, Autowired.class);

            // @Configuration类型的Bean是工厂，不允许@Autowired
            final boolean isConfiguration = isConfigurationDefinition(def);
            if (isConfiguration && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when create @Configuration bean '%s': %s.",
                                def.getName(), def.getBeanClass().getName()));
            }

            // BeanPostProcessor不能依赖其他Bean，不允许使用@autowired创建
            final boolean isBeanPostProcessor = isBeanPostProcessorDefinition(def);
            if (isBeanPostProcessor && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify @Autowired when create BeanPostProcessor '%s': %s.",
                                def.getName(), def.getBeanClass().getName()));
            }

            // 参数需要@Autowired或@Value两者之一
            if (value != null && autowired != null) {
                throw new BeanCreationException(
                        String.format("Cannot specify both @Autowired and @Value when create bean '%s': %s.",
                                def.getName(), def.getBeanClass().getName()));
            }
            if (value == null && autowired == null) {
                throw new BeanCreationException(
                        String.format("Must specify @Autowired or @Value when create bean '%s': %s.", def.getName(),
                                def.getBeanClass().getName()));
            }

            // 参数类型:
            final Class<?> type = param.getType();
            if (value != null) {
                // 参数为@Value
                args[i] = this.propertyResolver.getRequiredProperty(value.value(), type);
            } else {
                // 参数是@Autowired
                String name = autowired.name();
                boolean required = autowired.value();
                // 依赖的BeanDefinition
                BeanDefinition dependsOnDef = name.isEmpty() ? findBeanDefinition(type)
                        : findBeanDefinition(name, type);
                // 检测required==true?
                if (required && dependsOnDef == null) {
                    throw new BeanCreationException(String.format(
                            "Missing autowired bean with type '%s' when create bean '%s': %s.", type.getName(),
                            def.getName(), def.getBeanClass().getName()));
                }
                if (dependsOnDef != null) {
                    // 获取依赖Bean
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    if (autowiredBeanInstance == null) {
                        // 当前依赖Bean尚未初始化，递归调用初始化该依赖Bean:
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                } else {
                    args[i] = null;
                }
            }
        }

        // 创建Bean实例：
        Object instance;
        if (def.getFactoryName() == null) {
            // 构造方法
            try {
                assert def.getConstructor() != null;
                instance = def.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(),
                        def.getBeanClass().getName()), e);
            }
        } else {
            // @Bean方法
            Object configInstance = getBean(def.getFactoryName());
            try {
                assert def.getFactoryMethod() != null;
                instance = def.getFactoryMethod().invoke(configInstance, args);
            } catch (Exception e) {
                throw new BeanCreationException(String.format("Exception when create bean '%s': %s", def.getName(),
                        def.getBeanClass().getName()), e);
            }
        }
        def.setInstance(instance);
        // 调用BeanPostProcessor处理Bean
        for (BeanPostProcessor processor : beanPostProcessors) {
            Object processed = processor.postProcessBeforeInit(def.getInstance(), def.getName());
            if (processed == null) {
                throw new BeanCreationException(String.format(
                        "PostBeanProcessor returns null when process bean '%s' by %s", def.getName(), processor));
            }
            // 如果一个BeanPostProcessor替换了原始Bean，则更新Bean的引用
            if (def.getInstance() != processed) {
                logger.atDebug().log("Bean '{}' was replaced by post processor {}.", def.getName(),
                        processor.getClass().getName());
                def.setInstance(processed);
            }
        }

        return def.getInstance();
    }

    /*
     * 通过Name查找Bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        BeanDefinition def = this.beans.get(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with name '%s'.", name));
        }
        return (T) def.getRequiredInstance();
    }

    /*
     * 通过Name和Type查找Bean
     */
    public <T> T getBean(String name, Class<T> requiredType) {
        T t = findBean(name, requiredType);
        if (t == null) {
            throw new NoSuchBeanDefinitionException(
                    String.format("No bean defined with name '%s' and type '%s'.", name, requiredType));
        }
        return t;
    }

    /*
     * 通过Type查找Bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(String.format("No bean defined with type '%s'.", requiredType));
        }
        return (T) def.getRequiredInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> requiredType) {
        List<BeanDefinition> defs = findBeanDefinitions(requiredType);
        if (defs.isEmpty()) {
            return List.of();
        }
        List<T> list = new ArrayList<>(defs.size());
        for (BeanDefinition def : defs) {
            list.add((T) def.getRequiredInstance());
        }
        return list;
    }

    // find 与 get类似，但不存在时返回null

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> T findBean(String name, Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(name, requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T findBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> List<T> findBeans(Class<T> requiredType) {
        return findBeanDefinitions(requiredType).stream().map(def -> (T) def.getRequiredInstance())
                .collect(Collectors.toList());
    }

    public boolean containsBean(String name) {
        return this.beans.containsKey(name);
    }

    Map<String, BeanDefinition> createBeanDefinitions(Set<String> classNameSet) {
        Map<String, BeanDefinition> defs = new HashMap<>();
        for (String className : classNameSet) {
            // 获取Class
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException(e);
            }
            if (clazz.isAnnotation() || clazz.isEnum() || clazz.isInterface() || clazz.isRecord()) {
                continue;
            }
            // 是否标注@Component？
            Component component = ClassUtils.findAnnotation(clazz, Component.class);
            if (component != null) {
                logger.atDebug().log("found component: {}", clazz.getName());
                int mod = clazz.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Component class " + clazz.getName() + " must not be abstract.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Component class " + clazz.getName() + " must not be private.");
                }
                /* 获取Bean的名称 */
                String beanName = ClassUtils.getBeanName(clazz);
                BeanDefinition def = new BeanDefinition(beanName, clazz, getSuitableConstructor(clazz), getOrder(clazz),
                        clazz.isAnnotationPresent(Primary.class),
                        null, null,
                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinitions(defs, def);
                logger.atDebug().log("define bean: {}", def);

                /* 查找是否有@Configuration */
                Configuration configuration = ClassUtils.findAnnotation(clazz, Configuration.class);
                if (configuration != null) {
                    // 查找@Bean方法
                    scanFactoryMethods(beanName, clazz, defs);
                }
            }

        }

        return defs;
    }

    void scanFactoryMethods(String factoryBeanName, Class<?> clazz, Map<String, BeanDefinition> defs) {
        for (Method method : clazz.getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException(
                            "@Bean method " + clazz.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanDefinitionException(
                            "@Bean method " + clazz.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException(
                            "@Bean method " + clazz.getName() + "." + method.getName() + " must not be private.");
                }
                Class<?> beanClass = method.getReturnType();
                if (beanClass.isPrimitive()) {
                    throw new BeanDefinitionException("@Bean method " + clazz.getName() + "." + method.getName()
                            + " must not return primitive type.");
                }
                if (beanClass == void.class || beanClass == Void.class) {
                    throw new BeanDefinitionException(
                            "@Bean method " + clazz.getName() + "." + method.getName() + " must not return void.");
                }
                BeanDefinition def = new BeanDefinition(ClassUtils.getBeanName(method), beanClass, factoryBeanName,
                        method, getOrder(method),
                        method.isAnnotationPresent(Primary.class),
                        bean.initMethod().isEmpty() ? null : bean.initMethod(),
                        bean.destroyMethod().isEmpty() ? null : bean.destroyMethod(),
                        null, null);
                addBeanDefinitions(defs, def);
                logger.atDebug().log("define bean: {}", def);
            }
        }
    }
    
    int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    void addBeanDefinitions(Map<String, BeanDefinition> defs, BeanDefinition def) {
        if (defs.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    Constructor<?> getSuitableConstructor(Class<?> clazz) {
        Constructor<?>[] cons = clazz.getConstructors();
        if (cons.length == 0) {
            cons = clazz.getDeclaredConstructors();
            if (cons.length != 1) {
                throw new BeanDefinitionException("More than one constructor found in class " + clazz.getName() + ".");
            }
        }
        if (cons.length != 1) {
            throw new BeanDefinitionException(
                    "More than one public constructor found in class " + clazz.getName() + ".");
        }
        return cons[0];
    }

    /* 扫描指定包下的所有Class，然后返回Class名字 */
    protected Set<String> scanForClassNames(Class<?> configClass) {
        ComponentScan scan = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        /* 获取注解配置的package名字，未配置则默认当前类所在包 */
        final String[] scanPackages = scan == null || scan.value().length == 0
                ? new String[]{configClass.getPackage().getName()}
                : scan.value();
        logger.atInfo().log("component scan in package: {}", Arrays.toString(scanPackages));

        Set<String> classNameSet = new HashSet<>();
        /* 依次扫描所有package */
        for (String pkg : scanPackages) {
            logger.atDebug().log("scan package: {}", pkg);
            ResourceResolver rr = new ResourceResolver(pkg);
            List<String> classList = rr.scan(res -> {
                String name = res.name();
                if (name.endsWith(".class")) {
                    return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
                }
                return null;
            });
            if (logger.isDebugEnabled()) {
                classList.forEach(className -> logger.debug("class found by component scan: {}", className));
            }
            /* 扫描结果添加到set */
            classNameSet.addAll(classList);
        }

        /* 继续查找@Import(Xyz.class)导入的Class配置 */
        Import importConfig = configClass.getAnnotation(Import.class);
        if (importConfig != null) {
            for (Class<?> importConfigClass : importConfig.value()) {
                String importClassName = importConfigClass.getName();
                if (classNameSet.contains(importClassName)) {
                    logger.warn("ignore import: {} for it is already been scanned.", importClassName);
                } else {
                    logger.debug("class found by import: {}", importClassName);
                    classNameSet.add(importClassName);
                }
            }
        }

        return classNameSet;
    }

    boolean isConfigurationDefinition(BeanDefinition def) {
        return ClassUtils.findAnnotation(def.getBeanClass(), Configuration.class) != null;
    }

    boolean isBeanPostProcessorDefinition(BeanDefinition def) {
        return BeanPostProcessor.class.isAssignableFrom(def.getBeanClass());
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String name) {
        return this.beans.get(name);
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            return null;
        }
        if (!requiredType.isAssignableFrom(def.getBeanClass())) {
            throw new BeanNotOfRequiredTypeException(
                    String.format("Autowired required type '%s' but bean '%s' has actual type '%s'.",
                            requiredType.getName(), name, def.getBeanClass().getName()));
        }
        return def;
    }

    /* 根据Type查找若干个BeanDefinition, 返回0个或多个 */
    public List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return this.beans.values().stream()
                .filter(def -> type.isAssignableFrom(def.getBeanClass()))
                .sorted().collect(Collectors.toList());
    }

    /*
     * 根据Type查找某个BeanDefinition, 如果不存在返回null,
     * 如果存在多个则返回@primary标注的一个
     */
    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> type) {
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) { // 找到唯一一个
            return defs.get(0);
        }
        // 查找@Primary
        List<BeanDefinition> primaryDefs = defs.stream().filter(BeanDefinition::isPrimary).toList();
        if (primaryDefs.size() == 1) {
            return primaryDefs.get(0);
        }
        if (primaryDefs.isEmpty()) {
            throw new NoUniqueBeanDefinitionException(
                    String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new NoUniqueBeanDefinitionException(String
                    .format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }
    }

    @Override
    public void close() {
        logger.info("Closing {}...", this.getClass().getName());
        this.beans.values().forEach(def -> {
            final Object beanInstance = getProxiedInstance(def);
            callMethod(beanInstance, def.getDestroyMethod(), def.getDestroyMethodName());
        });
        this.beans.clear();
        logger.info("{} closed", this.getClass().getName());
        ApplicationContextUtils.setApplicationContext(null);
    }
}
