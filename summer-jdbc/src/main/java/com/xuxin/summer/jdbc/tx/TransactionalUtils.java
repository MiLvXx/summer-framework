package com.xuxin.summer.jdbc.tx;

import jakarta.annotation.Nullable;

import java.sql.Connection;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/3
 */
public class TransactionalUtils {
    @Nullable
    public static Connection getCurrentConnection() {
        TransactionStatus ts = DataSourceTransactionManager.transactionStatus.get();
        return ts == null ? null : ts.connection;
    }
}
