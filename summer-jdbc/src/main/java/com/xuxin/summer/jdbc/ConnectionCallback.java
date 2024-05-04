package com.xuxin.summer.jdbc;

import jakarta.annotation.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/5/3
 */
public interface ConnectionCallback<T> {

    @Nullable
    T doInConnection(Connection connection) throws SQLException;
}
