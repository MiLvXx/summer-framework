package com.xuxin.summer.jdbc.tx;

import java.sql.Connection;

/**
 * description:
 * 该类如果拓展，可封装事务传播模式
 * @author xuxin
 * @since 2024/5/3
 */
public class TransactionStatus {

    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }
}
