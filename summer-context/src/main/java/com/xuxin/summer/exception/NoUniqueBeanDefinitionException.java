package com.xuxin.summer.exception;

public class NoUniqueBeanDefinitionException extends BeanDefinitionException {
    
    public NoUniqueBeanDefinitionException() {}

    public NoUniqueBeanDefinitionException(String message) { super(message); }
}
