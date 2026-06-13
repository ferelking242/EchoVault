package com.aivos.echovault.data.repository

import com.aivos.echovault.data.db.dao.ClipboardDao
import com.aivos.echovault.data.db.dao.CollectionDao
import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
import com.aivos.echovault.data.db.entity.CollectionEntity
import com.aivos.echovault.data.db.entity.ContentType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao,
    private val collectionDao: CollectionDao
) {
    fun getAllEntries(): Flow<List<ClipboardEntryEntity>> = clipboardDao.getAllEntries()
    fun getFavorites(): Flow<List<ClipboardEntryEntity>> = clipboardDao.getFavoriteEntries()
    fun search(query: String): Flow<List<ClipboardEntryEntity>> = clipboardDao.searchEntries(query)
    fun getTotalCount(): Flow<Int> = clipboardDao.getTotalCount()
    fun getTodayCount(): Flow<Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return clipboardDao.getTodayCount(cal.timeInMillis)
    }
    fun getWeekEntries(): Flow<List<ClipboardEntryEntity>> {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return clipboardDao.getThisWeekEntries(cal.timeInMillis)
    }
    fun getEntriesByRange(start: Long, end: Long): Flow<List<ClipboardEntryEntity>> =
        clipboardDao.getEntriesByDateRange(start, end)
    fun getAllCollections(): Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

    suspend fun addEntry(content: String): Long {
        val existing = clipboardDao.findByContent(content)
        if (existing != null) {
            clipboardDao.updateEntry(existing.copy(timestamp = System.currentTimeMillis()))
            return existing.id
        }
        return clipboardDao.insertEntry(
            ClipboardEntryEntity(content = content, contentType = detectContentType(content))
        )
    }

    suspend fun toggleFavorite(entry: ClipboardEntryEntity) {
        clipboardDao.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
    }
    suspend fun togglePin(entry: ClipboardEntryEntity) {
        clipboardDao.updateEntry(entry.copy(isPinned = !entry.isPinned, isFavorite = true))
    }
    suspend fun deleteEntry(entry: ClipboardEntryEntity) = clipboardDao.deleteEntry(entry)
    suspend fun deleteAll() = clipboardDao.deleteAll()
    suspend fun getEntryById(id: Long) = clipboardDao.getEntryById(id)
    suspend fun getTotalDatabaseSize() = clipboardDao.getTotalDatabaseSize() ?: 0L

    suspend fun createCollection(name: String, color: Long) =
        collectionDao.insertCollection(CollectionEntity(name = name, color = color))

    private fun detectContentType(content: String): ContentType {
        val t = content.trim()
        return when {
            t.matches(Regex("""^\d{4,8}$""")) -> ContentType.OTP
            t.matches(Regex("""^[+]?\d[\d\s\-().]{6,}$""")) -> ContentType.PHONE
            t.contains("@") && t.contains(".") -> ContentType.EMAIL
            t.startsWith("http://") || t.startsWith("https://") || t.startsWith("www.") -> ContentType.URL
            t.lines().size > 3 -> ContentType.MULTILINE
            t.contains("{") || t.contains("fun ") || t.contains("def ") || t.contains("class ") -> ContentType.CODE
            else -> ContentType.TEXT
        }
    }
}