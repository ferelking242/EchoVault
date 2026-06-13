package com.aivos.echovault.domain.model

  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.db.entity.ContentType

  data class ClipEntry(
      val id: Long,
      val content: String,
      val contentType: ContentType,
      val timestamp: Long,
      val charCount: Int,
      val isFavorite: Boolean,
      val isPinned: Boolean,
      val collectionId: Long?
  )

  fun ClipboardEntryEntity.toDomain() = ClipEntry(
      id = id, content = content, contentType = contentType,
      timestamp = timestamp, charCount = charCount,
      isFavorite = isFavorite, isPinned = isPinned, collectionId = collectionId
  )