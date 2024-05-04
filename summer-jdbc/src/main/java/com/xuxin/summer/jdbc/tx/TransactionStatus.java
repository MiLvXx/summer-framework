package com.xuxin.summer.jdbc.tx;

import java.sql.Connection;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/3
 */
public class TransactionStatus {

    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }
}
