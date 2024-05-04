package com.xuxin.summer.exception;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/2
 */
public class DataAccessException extends NestedRuntimeException{
    public DataAccessException() { }

    public DataAccessException(String message, Throwable cause) { super(message, cause); }

    public DataAccessException(String message) { super(message); }

    public DataAccessException(Throwable cause) { super(cause); }
}
