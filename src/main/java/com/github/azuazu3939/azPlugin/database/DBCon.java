package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Objects;

public class DBCon {

    protected static HikariDataSource dataSource;

    protected static String LOCATION;

    public static void init() throws SQLException {
        if (!AzPlugin.getInstance().getConfig().getBoolean("Database.use")) return;
        LOCATION = AzPlugin.getInstance().getConfig().getString("Database.location");

        new org.mariadb.jdbc.Driver();
        HikariConfig config = new HikariConfig();
        String host = AzPlugin.getInstance().getConfig().getString("Database.host");
        int port = AzPlugin.getInstance().getConfig().getInt("Database.port");
        String database = AzPlugin.getInstance().getConfig().getString("Database.database");
        String username = AzPlugin.getInstance().getConfig().getString("Database.username");
        String password = AzPlugin.getInstance().getConfig().getString("Database.password");
        String scheme = AzPlugin.getInstance().getConfig().getString("Database.scheme");

        config.setJdbcUrl(scheme + "://" + host + ":" + port + "/" + database);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(12);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
        createTables();
    }

    public static void createTables() throws SQLException {
        runPrepareStatement("CREATE TABLE IF NOT EXISTS `" + LOCATION + "` (\n" +
                "`name` varchar(36) NOT NULL, \n" +
                "`x` int, \n" +
                "`y` smallint, \n" +
                "`z` int, \n" +
                "`tick` int DEFAULT 200, \n" +
                "`mmid` varchar(128) NOT NULL, \n" +
                "`amount` tinyint,  \n" +
                "`material` varchar(32), \n" +
                "`chance` double, \n" +
                "`ct_material` varchar(32), \n" +
                "PRIMARY KEY (`name`, `x`, `y`, `z`)\n" +
                ")", PreparedStatement::execute);

    }

    public static HikariDataSource getDataSource() {
        return Objects.requireNonNull(dataSource, "#init was not called");
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Contract(pure = true)
    public static <R> R use(@NotNull SQLThrowableFunction<Connection, R> action) throws SQLException {
        try (Connection connection = getConnection()) {
            return action.apply(connection);
        }
    }

    @Contract(pure = true)
    public static void use(@NotNull SQLThrowableConsumer<Connection> action) throws SQLException {
        try (Connection con = getConnection()) {
            action.accept(con);
        }
    }

    @Contract(pure = true)
    public static void runPrepareStatement(@Language("SQL") @NotNull String sql, SQLThrowableConsumer<PreparedStatement> action) throws SQLException {
        use(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                action.accept(preparedStatement);
            }
        });
    }

    @Contract(pure = true)
    public static <R> R getPrepareStatement(@Language("SQL") @NotNull String sql, @NotNull SQLThrowableFunction<PreparedStatement, R> action) throws SQLException {
        return use(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                return action.apply(preparedStatement);
            }
        });
    }

    @Contract(pure = true)
    public static void useStatement(@NotNull SQLThrowableConsumer<Statement> action) throws SQLException {
        use(connection -> {
            try (Statement statement = connection.createStatement()) {
                action.accept(statement);
            }
        });
    }
}
