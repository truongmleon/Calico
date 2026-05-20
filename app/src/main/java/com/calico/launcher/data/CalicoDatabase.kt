package com.calico.launcher.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CalicoDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON")
        SCHEMA.forEach(db::execSQL)
        seedPlatformsAndEmulators(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // The app is pre-release, so schema changes can be replaced cleanly until migrations ship.
        TABLES.reversed().forEach { table -> db.execSQL("DROP TABLE IF EXISTS $table") }
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    private fun seedPlatformsAndEmulators(db: SQLiteDatabase) {
        DEFAULT_EMULATORS.forEach { emulator ->
            db.execSQL(
                """
                INSERT INTO emulators (emulator_id, display_name, package_name, install_url, is_enabled)
                VALUES (?, ?, ?, ?, 1)
                """.trimIndent(),
                arrayOf(emulator.id, emulator.displayName, emulator.packageName, emulator.installUrl),
            )
        }

        DEFAULT_PLATFORMS.forEach { platform ->
            db.execSQL(
                """
                INSERT INTO platforms (
                    platform_id, name, rom_folder_name, supported_extensions, default_emulator_id
                ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf(
                    platform.id,
                    platform.name,
                    platform.romFolderName,
                    platform.supportedExtensions.joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\""),
                    platform.defaultEmulatorId,
                ),
            )
        }
    }

    companion object {
        private const val DATABASE_NAME = "metadata.db"
        private const val DATABASE_VERSION = 1

        val TABLES = listOf(
            "game_genres_map",
            "game_developers_map",
            "game_publishers_map",
            "assets",
            "input_bindings",
            "web_bookmarks",
            "music_album_tracks",
            "music_albums",
            "music_tracks",
            "taskbar_items",
            "settings",
            "scan_events",
            "scan_roots",
            "play_sessions",
            "collection_games",
            "collections",
            "game_emulator_preferences",
            "emulator_platforms",
            "game_files",
            "games",
            "genres",
            "developers",
            "publishers",
            "platforms",
            "emulators",
        )

        val SCHEMA = listOf(
            """
            CREATE TABLE emulators (
                emulator_id INTEGER PRIMARY KEY,
                display_name TEXT NOT NULL,
                package_name TEXT NOT NULL,
                install_url TEXT NOT NULL,
                is_enabled INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
            """
            CREATE TABLE platforms (
                platform_id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                rom_folder_name TEXT NOT NULL UNIQUE,
                supported_extensions TEXT NOT NULL,
                default_emulator_id INTEGER NOT NULL,
                FOREIGN KEY(default_emulator_id) REFERENCES emulators(emulator_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE games (
                game_id INTEGER PRIMARY KEY AUTOINCREMENT,
                platform_id INTEGER NOT NULL,
                sort_title TEXT NOT NULL,
                screenscraper_id INTEGER UNIQUE,
                steamgriddb_id INTEGER UNIQUE,
                retroachievements_id INTEGER UNIQUE,
                last_played_at TEXT,
                game_name TEXT NOT NULL,
                game_desc TEXT,
                release_date TEXT,
                manual_metadata TEXT,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                is_favorite INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(platform_id) REFERENCES platforms(platform_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE game_files (
                file_id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER NOT NULL,
                platform_id INTEGER NOT NULL,
                file_type TEXT NOT NULL DEFAULT 'base',
                file_path TEXT NOT NULL,
                file_name TEXT NOT NULL,
                extension TEXT NOT NULL,
                size_bytes INTEGER,
                crc32 TEXT,
                md5 TEXT,
                sha1 TEXT,
                region TEXT,
                version TEXT,
                last_seen_at TEXT,
                added_at TEXT,
                content_uri TEXT,
                disc_number INTEGER,
                disc_total INTEGER,
                is_primary INTEGER NOT NULL DEFAULT 1,
                is_missing INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                FOREIGN KEY(platform_id) REFERENCES platforms(platform_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE emulator_platforms (
                emulator_platform_id INTEGER PRIMARY KEY AUTOINCREMENT,
                platform_id INTEGER NOT NULL,
                emulator_id INTEGER NOT NULL,
                supports_content_uri INTEGER NOT NULL DEFAULT 0,
                supports_file_uri INTEGER NOT NULL DEFAULT 1,
                direct_launch_supported INTEGER NOT NULL DEFAULT 1,
                requires_manual_import INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(platform_id) REFERENCES platforms(platform_id),
                FOREIGN KEY(emulator_id) REFERENCES emulators(emulator_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE game_emulator_preferences (
                game_id INTEGER NOT NULL,
                emulator_id INTEGER NOT NULL,
                platform_id INTEGER NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(game_id, emulator_id),
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                FOREIGN KEY(emulator_id) REFERENCES emulators(emulator_id),
                FOREIGN KEY(platform_id) REFERENCES platforms(platform_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE collections (
                collection_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                icon_path TEXT,
                sort_order INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE collection_games (
                collection_id INTEGER NOT NULL,
                game_id INTEGER NOT NULL,
                sort_order INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(collection_id, game_id),
                FOREIGN KEY(collection_id) REFERENCES collections(collection_id) ON DELETE CASCADE,
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE play_sessions (
                session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER NOT NULL,
                emulator_id INTEGER NOT NULL,
                started_at TEXT NOT NULL,
                ended_at TEXT,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(game_id) REFERENCES games(game_id),
                FOREIGN KEY(emulator_id) REFERENCES emulators(emulator_id)
            )
            """.trimIndent(),
            """
            CREATE TABLE scan_roots (
                root_id INTEGER PRIMARY KEY AUTOINCREMENT,
                root_path TEXT NOT NULL,
                roms_path TEXT NOT NULL,
                media_path TEXT NOT NULL,
                db_path TEXT NOT NULL,
                last_scanned_at TEXT
            )
            """.trimIndent(),
            """
            CREATE TABLE scan_events (
                scan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                started_at TEXT NOT NULL,
                ended_at TEXT,
                status TEXT NOT NULL,
                message TEXT
            )
            """.trimIndent(),
            """
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT
            )
            """.trimIndent(),
            """
            CREATE TABLE taskbar_items (
                item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                package_name TEXT,
                icon_path TEXT,
                item_type TEXT NOT NULL,
                sort_order INTEGER NOT NULL DEFAULT 0,
                is_enabled INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
            """
            CREATE TABLE music_tracks (
                track_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                artist TEXT,
                file_path TEXT,
                content_uri TEXT,
                duration_seconds INTEGER NOT NULL DEFAULT 0,
                is_favorite INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
            """
            CREATE TABLE music_albums (
                album_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                icon_path TEXT,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
            """
            CREATE TABLE music_album_tracks (
                album_id INTEGER NOT NULL,
                track_id INTEGER NOT NULL,
                sort_order INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(album_id, track_id),
                FOREIGN KEY(album_id) REFERENCES music_albums(album_id) ON DELETE CASCADE,
                FOREIGN KEY(track_id) REFERENCES music_tracks(track_id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE web_bookmarks (
                bookmark_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                url TEXT NOT NULL,
                icon_path TEXT,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
            """
            CREATE TABLE input_bindings (
                binding_id INTEGER PRIMARY KEY AUTOINCREMENT,
                action TEXT NOT NULL,
                key_code INTEGER NOT NULL,
                controller_name TEXT
            )
            """.trimIndent(),
            """
            CREATE TABLE publishers (
                publisher_id INTEGER PRIMARY KEY AUTOINCREMENT,
                publisher_name TEXT NOT NULL UNIQUE
            )
            """.trimIndent(),
            """
            CREATE TABLE developers (
                developer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                developer_name TEXT NOT NULL UNIQUE
            )
            """.trimIndent(),
            """
            CREATE TABLE genres (
                genre_id INTEGER PRIMARY KEY AUTOINCREMENT,
                genre_name TEXT NOT NULL UNIQUE
            )
            """.trimIndent(),
            """
            CREATE TABLE game_publishers_map (
                game_id INTEGER NOT NULL,
                publisher_id INTEGER NOT NULL,
                PRIMARY KEY(game_id, publisher_id),
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                FOREIGN KEY(publisher_id) REFERENCES publishers(publisher_id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE game_developers_map (
                game_id INTEGER NOT NULL,
                developer_id INTEGER NOT NULL,
                PRIMARY KEY(game_id, developer_id),
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                FOREIGN KEY(developer_id) REFERENCES developers(developer_id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE game_genres_map (
                game_id INTEGER NOT NULL,
                genre_id INTEGER NOT NULL,
                PRIMARY KEY(game_id, genre_id),
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE,
                FOREIGN KEY(genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
            )
            """.trimIndent(),
            """
            CREATE TABLE assets (
                asset_id INTEGER PRIMARY KEY AUTOINCREMENT,
                asset_scope TEXT NOT NULL,
                asset_type TEXT NOT NULL,
                game_id INTEGER,
                file_name TEXT NOT NULL,
                type TEXT NOT NULL,
                provider TEXT NOT NULL,
                local_path TEXT,
                source_url TEXT,
                width INTEGER,
                height INTEGER,
                aspect_ratio TEXT,
                is_selected INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0,
                content_uri TEXT,
                FOREIGN KEY(game_id) REFERENCES games(game_id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
    }
}
