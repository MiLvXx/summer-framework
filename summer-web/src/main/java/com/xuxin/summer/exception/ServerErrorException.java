package com.xuxin.summer.exception;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class ServerErrorException extends ErrorResponseException {
    public ServerErrorException() {
        super(500);
    }

    public ServerErrorException(String message) {
        super(500, message);
    }

    public ServerErrorException(Throwable cause) {
        super(500, cause);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(500, message, cause);
    }
}
