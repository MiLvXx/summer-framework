package com.xuxin.summer.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.xuxin.summer.annotation.Component;
import com.xuxin.summer.annotation.Configuration;
import com.xuxin.summer.annotation.Order;
import com.xuxin.summer.exception.BeanDefinitionException;

public class AnnoUtilsTest {

    @Test
    public void noComponent() {
        assertNull(ClassUtils.findAnnotation(Simple.class, Component.class));
    }

    @Test
    public void simpleComponent() {
        assertNotNull(ClassUtils.findAnnotation(SimpleComponent.class, Component.class));
        assertEquals("simpleComponent", ClassUtils.getBeanName(SimpleComponent.class));
    }

    @Test
    public void simpleComponentWithName() {
        assertNotNull(ClassUtils.findAnnotation(SimpleComponentWithName.class, Component.class));
        assertEquals("simpleName", ClassUtils.getBeanName(SimpleComponentWithName.class));
    }

    @Test
    public void simpleConfiguration() {
        assertNotNull(ClassUtils.findAnnotation(SimpleConfiguration.class, Component.class));
        assertEquals("simpleConfiguration", ClassUtils.getBeanName(SimpleConfiguration.class));
    }

    @Test
    public void simpleConfigurationWithName() {
        assertNotNull(ClassUtils.findAnnotation(SimpleConfigurationWithName.class, Component.class));
        assertEquals("simpleCfg", ClassUtils.getBeanName(SimpleConfigurationWithName.class));
    }

    @Test
    public void customComponent() {
        assertNotNull(ClassUtils.findAnnotation(Custom.class, Component.class));
        assertEquals("custom", ClassUtils.getBeanName(Custom.class));
    }

    @Test
    public void customComponentWithName() {
        assertNotNull(ClassUtils.findAnnotation(CustomWithName.class, Component.class));
        assertEquals("customName", ClassUtils.getBeanName(CustomWithName.class));
    }

    @Test
    public void duplicateComponent() {
        assertThrows(BeanDefinitionException.class, () -> ClassUtils.findAnnotation(DuplicateComponent.class, Component.class));
        assertThrows(BeanDefinitionException.class, () -> ClassUtils.findAnnotation(DuplicateComponent2.class, Component.class));
    }
}

@Order(1)
class Simple {
}

@Component
class SimpleComponent {
}

@Component("simpleName")
class SimpleComponentWithName {
}

@Configuration
class SimpleConfiguration {

}

@Configuration("simpleCfg")
class SimpleConfigurationWithName {

}

@CustomComponent
class Custom {

}

@CustomComponent("customName")
class CustomWithName {

}

@Component
@Configuration
class DuplicateComponent {

}

@CustomComponent
@Configuration
class DuplicateComponent2 {

}
