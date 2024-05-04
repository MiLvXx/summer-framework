package com.xuxin.summer.exception;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/2
 */
public class TransactionException extends DataAccessException{
    public TransactionException() { }

    public TransactionException(String message, Throwable cause) { super(message, cause); }

    public TransactionException(String message) { super(message); }

    public TransactionException(Throwable cause) { super(cause); }
}
