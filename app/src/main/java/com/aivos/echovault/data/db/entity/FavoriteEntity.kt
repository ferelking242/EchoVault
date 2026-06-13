package com.aivos.echovault.data.db.entity

  import androidx.room.Entity
  import androidx.room.PrimaryKey

  @Entity(tableName = "favorites")
  data class FavoriteEntity(
      @PrimaryKey val entryId: Long,
      val pinnedAt: Long = System.currentTimeMillis()
  )