package com.xuxin.summer.exception;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class ServerWebInputException extends ErrorResponseException {
    public ServerWebInputException() {
        super(400);
    }

    public ServerWebInputException(String message) {
        super(400, message);
    }

    public ServerWebInputException(Throwable cause) {
        super(400, cause);
    }

    public ServerWebInputException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
