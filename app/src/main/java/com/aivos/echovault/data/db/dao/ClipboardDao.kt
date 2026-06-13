package com.aivos.echovault.data.db.dao

  import androidx.room.*
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.db.entity.ContentType
  import kotlinx.coroutines.flow.Flow

  @Dao
  interface ClipboardDao {

      @Query("SELECT * FROM clipboard_entries ORDER BY timestamp DESC")
      fun getAllEntries(): Flow<List<ClipboardEntryEntity>>

      @Query("SELECT * FROM clipboard_entries WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
      fun getEntriesByDateRange(start: Long, end: Long): Flow<List<ClipboardEntryEntity>>

      @Query("SELECT * FROM clipboard_entries WHERE isFavorite = 1 ORDER BY isPinned DESC, timestamp DESC")
      fun getFavoriteEntries(): Flow<List<ClipboardEntryEntity>>

      @Query("SELECT * FROM clipboard_entries WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
      fun searchEntries(query: String): Flow<List<ClipboardEntryEntity>>

      @Query("SELECT * FROM clipboard_entries WHERE id = :id")
      suspend fun getEntryById(id: Long): ClipboardEntryEntity?

      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insertEntry(entry: ClipboardEntryEntity): Long

      @Update
      suspend fun updateEntry(entry: ClipboardEntryEntity)

      @Delete
      suspend fun deleteEntry(entry: ClipboardEntryEntity)

      @Query("DELETE FROM clipboard_entries WHERE id = :id")
      suspend fun deleteById(id: Long)

      @Query("DELETE FROM clipboard_entries")
      suspend fun deleteAll()

      @Query("SELECT COUNT(*) FROM clipboard_entries")
      fun getTotalCount(): Flow<Int>

      @Query("SELECT COUNT(*) FROM clipboard_entries WHERE timestamp >= :todayStart")
      fun getTodayCount(todayStart: Long): Flow<Int>

      @Query("SELECT contentType, COUNT(*) as cnt FROM clipboard_entries GROUP BY contentType ORDER BY cnt DESC")
      fun getContentTypeStats(): Flow<List<ContentTypeCount>>

      @Query("SELECT * FROM clipboard_entries WHERE content = :content ORDER BY timestamp DESC LIMIT 1")
      suspend fun findByContent(content: String): ClipboardEntryEntity?

      @Query("SELECT SUM(LENGTH(content)) FROM clipboard_entries")
      suspend fun getTotalDatabaseSize(): Long?

      @Query("SELECT * FROM clipboard_entries WHERE timestamp >= :weekStart ORDER BY timestamp DESC")
      fun getThisWeekEntries(weekStart: Long): Flow<List<ClipboardEntryEntity>>
  }

  data class ContentTypeCount(val contentType: ContentType, val cnt: Int)