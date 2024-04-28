package com.xuxin.summer.exception;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/4/28
 */
public class AopConfigException extends NestedRuntimeException {
    public AopConfigException() {
        super();
    }

    public AopConfigException(String message) {
        super(message);
    }

    public AopConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopConfigException(Throwable cause) {
        super(cause);
    }
}
