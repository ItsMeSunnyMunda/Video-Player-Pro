package xyz.mpv.rex.database

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.mpv.rex.database.dao.HybridMediaDao
import xyz.mpv.rex.database.dao.PlaybackStateDao
import xyz.mpv.rex.database.dao.PlaylistDao
import xyz.mpv.rex.database.dao.RecentlyPlayedDao
import xyz.mpv.rex.database.dao.VideoMetadataDao
import xyz.mpv.rex.database.entities.HybridMediaEntity
import xyz.mpv.rex.database.entities.HybridMediaRootEntity
import xyz.mpv.rex.database.entities.PlaybackStateEntity
import xyz.mpv.rex.database.entities.PlaylistEntity
import xyz.mpv.rex.database.entities.PlaylistItemEntity
import xyz.mpv.rex.database.entities.RecentlyPlayedEntity
import xyz.mpv.rex.database.dao.MediaPlayCountDao
import xyz.mpv.rex.database.entities.MediaPlayCountEntity
import xyz.mpv.rex.database.entities.VideoMetadataEntity

@Database(
  entities = [
    PlaybackStateEntity::class,
    RecentlyPlayedEntity::class,
    VideoMetadataEntity::class,
    PlaylistEntity::class,
    PlaylistItemEntity::class,
    HybridMediaEntity::class,
    HybridMediaRootEntity::class,
    MediaPlayCountEntity::class,
  ],
  version = 18,
  exportSchema = true,
)
abstract class MpvExDatabase : RoomDatabase() {
  abstract fun videoDataDao(): PlaybackStateDao

  abstract fun recentlyPlayedDao(): RecentlyPlayedDao

  abstract fun videoMetadataDao(): VideoMetadataDao

  abstract fun playlistDao(): PlaylistDao

  abstract fun hybridMediaDao(): HybridMediaDao

  abstract fun mediaPlayCountDao(): MediaPlayCountDao
}
