package com.aivos.echovault.data.db.entity

  import androidx.room.Entity
  import androidx.room.PrimaryKey

  @Entity(tableName = "collections")
  data class CollectionEntity(
      @PrimaryKey(autoGenerate = true) val id: Long = 0,
      val name: String,
      val color: Long = 0xFF6200EE,
      val createdAt: Long = System.currentTimeMillis()
  )