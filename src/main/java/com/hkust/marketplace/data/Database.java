package com.hkust.marketplace.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * sqlite connection factory and schema owner
 * the database file lives at data/marketplace.db by default
 */
public final class Database {

    public static final String DEFAULT_URL = "jdbc:sqlite:data/marketplace.db";

    private static String url = System.getProperty("marketplace.db.url", DEFAULT_URL);
    private static boolean schemaReady;

    static {
        // make sure the driver is registered even outside the maven launcher
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("sqlite jdbc driver is missing", ex);
        }
    }

    private static final String[] SCHEMA = {
        """
        CREATE TABLE IF NOT EXISTS users (
            username TEXT PRIMARY KEY,
            full_name TEXT NOT NULL,
            password_hash TEXT NOT NULL,
            role TEXT NOT NULL,
            residence_hall TEXT,
            bio TEXT,
            preferred_meetup TEXT,
            active INTEGER NOT NULL DEFAULT 1,
            created_at TEXT,
            average_rating REAL NOT NULL DEFAULT 0,
            rating_count INTEGER NOT NULL DEFAULT 0,
            verified INTEGER NOT NULL DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS listings (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT,
            category TEXT,
            type TEXT,
            status TEXT,
            condition_text TEXT,
            price REAL,
            lending_days INTEGER,
            meetup_location TEXT,
            seller_username TEXT,
            seller_full_name TEXT,
            posted_at TEXT,
            reviewed_at TEXT,
            rejection_reason TEXT,
            view_count INTEGER NOT NULL DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS listing_photos (
            listing_id TEXT NOT NULL,
            position INTEGER NOT NULL,
            path TEXT NOT NULL,
            PRIMARY KEY (listing_id, position)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS offers (
            id TEXT PRIMARY KEY,
            listing_id TEXT,
            buyer_username TEXT,
            seller_username TEXT,
            type TEXT,
            status TEXT,
            offer_price REAL,
            borrow_start TEXT,
            borrow_end TEXT,
            message TEXT,
            counter_price REAL,
            counter_end TEXT,
            created_at TEXT,
            responded_at TEXT
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS chat_threads (
            id TEXT PRIMARY KEY,
            listing_id TEXT,
            listing_title TEXT,
            buyer_username TEXT,
            seller_username TEXT,
            created_at TEXT
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS chat_messages (
            id TEXT PRIMARY KEY,
            thread_id TEXT,
            sender_username TEXT,
            content TEXT,
            sent_at TEXT,
            is_formal_offer INTEGER NOT NULL DEFAULT 0,
            offer_id TEXT
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS deals (
            id TEXT PRIMARY KEY,
            listing_id TEXT,
            listing_title TEXT,
            buyer_username TEXT,
            seller_username TEXT,
            deal_type TEXT,
            status TEXT,
            offer_price REAL,
            borrow_start TEXT,
            borrow_end TEXT,
            created_at TEXT,
            completed_at TEXT,
            due_at TEXT,
            rated INTEGER NOT NULL DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS notifications (
            id TEXT PRIMARY KEY,
            username TEXT,
            title TEXT,
            message TEXT,
            category TEXT,
            timestamp TEXT,
            read_flag INTEGER NOT NULL DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS reviews (
            id TEXT PRIMARY KEY,
            seller_username TEXT,
            buyer_username TEXT,
            listing_title TEXT,
            rating INTEGER NOT NULL DEFAULT 0,
            comment TEXT,
            seller_reply TEXT,
            hidden INTEGER NOT NULL DEFAULT 0,
            created_at TEXT,
            flagged INTEGER NOT NULL DEFAULT 0,
            flag_reason TEXT,
            deal_id TEXT
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS reports (
            id TEXT PRIMARY KEY,
            listing_id TEXT,
            reporter_username TEXT,
            reason TEXT,
            created_at TEXT,
            resolved INTEGER NOT NULL DEFAULT 0
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS favourites (
            username TEXT NOT NULL,
            listing_id TEXT NOT NULL,
            PRIMARY KEY (username, listing_id)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS audit_log (
            position INTEGER PRIMARY KEY,
            line TEXT NOT NULL
        )
        """
    };

    private Database() {
    }

    public static synchronized void setUrlForTests(String testUrl) {
        url = testUrl;
        schemaReady = false;
    }

    public static synchronized String getUrl() {
        return url;
    }

    public static Connection getConnection() throws SQLException {
        ensureSchema();
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    private static synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        createParentDirectory();
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            for (String ddl : SCHEMA) {
                statement.execute(ddl);
            }
            schemaReady = true;
        } catch (SQLException ex) {
            throw new IllegalStateException("could not create the marketplace database", ex);
        }
    }

    private static void createParentDirectory() {
        if (!url.startsWith("jdbc:sqlite:") || url.contains(":memory:")) {
            return;
        }
        String path = url.substring("jdbc:sqlite:".length());
        Path parent = Paths.get(path).toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException ex) {
            throw new IllegalStateException("could not create the database directory", ex);
        }
    }
}
