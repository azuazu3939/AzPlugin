package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DBCon {

    protected static HikariDataSource dataSource;

    protected static String BREAK;
    protected static String INTERACT;
    protected static String INVENTORY;

    protected static final Map<AbstractLocationSet, Integer> LOCATION_SET = new ConcurrentHashMap<>();

    public static void init() throws SQLException {
        if (!AzPlugin.getInstance().getConfig().getBoolean("Database.use")) return;
        BREAK = AzPlugin.getInstance().getConfig().getString("Database.break");
        INTERACT = AzPlugin.getInstance().getConfig().getString("Database.interact");
        INVENTORY = AzPlugin.getInstance().getConfig().getString("Database.inventory");

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
        runPrepareStatement("CREATE TABLE IF NOT EXISTS `" + BREAK + "` (\n" +
                "`name` varchar(36) NOT NULL, \n" +
                "`x` int, \n" +
                "`y` smallint, \n" +
                "`z` int, \n" +
                "`tick` int DEFAULT 200, \n" +
                "`mmid` varchar(128) NOT NULL, \n" +
                "`amount` tinyint,  \n" +
                "`chance` double, \n" +
                "`ct_material` varchar(32), \n" +
                "PRIMARY KEY (`name`, `x`, `y`, `z`)\n" +
                ")", PreparedStatement::execute);
        runPrepareStatement("CREATE TABLE IF NOT EXISTS `" + INTERACT + "` (\n" +
                "`name` varchar(36) NOT NULL , \n" +
                "`x` int, \n" +
                "`y` smallint, \n" +
                "`z` int, \n" +
                "`shop` varchar(36) NOT NULL, \n" +
                "PRIMARY KEY (`name`, `x`, `y`, `z`)\n" +
                ")", PreparedStatement::execute);
        runPrepareStatement("CREATE TABLE IF NOT EXISTS `" + INVENTORY + "` (\n" +
                "`shop` varchar(36) NOT NULL , \n" +
                "`slot` tinyint(54), \n" +
                "`item` blob, \n" +
                "`cursor` blob, \n" +
                "PRIMARY KEY (`shop`, `slot`)\n" +
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

    public static void loadBreak() {
        AzPlugin.getInstance().runAsyncLater(() -> {
            try {
                runPrepareStatement("SELECT `name`, `x`, `y`, `z` FROM `" + BREAK + "`;", (preparedStatement) -> {
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            World world = Bukkit.getWorld(rs.getString("name"));
                            if (world == null) continue;
                            LOCATION_SET.put(new AbstractLocationSet(
                                    world,
                                    rs.getInt("x"),
                                    rs.getInt("y"),
                                    rs.getInt("z")),
                                    1);
                        }
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 100L);
    }

    public static void loadInteract() {
        AzPlugin.getInstance().runAsyncLater(() -> {
            try {
                runPrepareStatement("SELECT `name`, `x`, `y`, `z` FROM `" + INTERACT + "`;", (preparedStatement) -> {
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            World world = Bukkit.getWorld(rs.getString("name"));
                            if (world == null) continue;
                            LOCATION_SET.put(new AbstractLocationSet(
                                    world,
                                    rs.getInt("x"),
                                    rs.getInt("y"),
                                    rs.getInt("z")),
                                    2);
                        }
                    }
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 200L);
    }

    public record AbstractLocationSet(World world, int x, int y, int z) {

        @NotNull
        @Contract("_ -> new")
        public static AbstractLocationSet create(@NotNull Location loc) {
            return new AbstractLocationSet(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    @Nullable
    public static AbstractLocationSet getLocationSet(Location location) {
         if (LOCATION_SET.keySet().stream().anyMatch(l -> {
            if (!l.world.getName().equals(location.getWorld().getName())) return false;
            return l.x == location.getBlockX() && l.y == location.getBlockY() && l.z == location.getBlockZ();

        })) {
             return new AbstractLocationSet(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
         }
        return null;
    }

    public static int locationToInt(AbstractLocationSet set) {
        if (LOCATION_SET.containsKey(set)) return LOCATION_SET.get(set);
        return 0;
    }
}
