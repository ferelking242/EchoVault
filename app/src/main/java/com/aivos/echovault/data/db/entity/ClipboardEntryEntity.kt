package com.aivos.echovault.data.db.entity

  import androidx.room.Entity
  import androidx.room.Index
  import androidx.room.PrimaryKey

  enum class ContentType { TEXT, URL, EMAIL, PHONE, OTP, CODE, MULTILINE }

  @Entity(
      tableName = "clipboard_entries",
      indices = [Index("timestamp"), Index("isFavorite"), Index("contentType")]
  )
  data class ClipboardEntryEntity(
      @PrimaryKey(autoGenerate = true) val id: Long = 0,
      val content: String,
      val contentType: ContentType = ContentType.TEXT,
      val timestamp: Long = System.currentTimeMillis(),
      val charCount: Int = content.length,
      val isFavorite: Boolean = false,
      val isPinned: Boolean = false,
      val collectionId: Long? = null
  )