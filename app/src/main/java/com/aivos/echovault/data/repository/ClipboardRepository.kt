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
              isOtp(t) -> ContentType.OTP
              isPhone(t) -> ContentType.PHONE
              isEmail(t) -> ContentType.EMAIL
              isUrl(t) -> ContentType.URL
              t.lines().size > 3 -> ContentType.MULTILINE
              isCode(t) -> ContentType.CODE
              else -> ContentType.TEXT
          }
      }

      private fun isOtp(t: String): Boolean {
          if (t.length < 4 || t.length > 8) return false
          return t.all { it.isDigit() }
      }

      private fun isPhone(t: String): Boolean {
          if (t.length < 7) return false
          val digits = t.filter { it.isDigit() }
          return digits.length in 7..15 && t.first().let { it.isDigit() || it == '+' }
      }

      private fun isEmail(t: String): Boolean {
          val atIdx = t.indexOf('@')
          return atIdx > 0 && t.indexOf('.', atIdx) > atIdx + 1 && !t.contains(' ')
      }

      private fun isUrl(t: String): Boolean {
          return t.startsWith("http://") || t.startsWith("https://") || t.startsWith("www.")
      }

      private fun isCode(t: String): Boolean {
          return t.contains("{") || t.contains("fun ") || t.contains("def ") ||
                 t.contains("class ") || t.contains("import ") || t.contains("return ")
      }
  }