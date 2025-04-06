package com.github.azuazu3939.azPlugin.database;

import java.sql.SQLException;

public interface SQLThrowableConsumer<T> {
    void accept(T t) throws SQLException;
}
