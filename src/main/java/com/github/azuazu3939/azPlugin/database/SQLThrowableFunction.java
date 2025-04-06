package com.github.azuazu3939.azPlugin.database;

import org.jetbrains.annotations.Contract;

import java.sql.SQLException;

public interface SQLThrowableFunction<T, R> {
    @Contract(pure = true)
    R apply(T t) throws SQLException;
}
