package com.aivos.echovault.data.db

  import androidx.room.Database
  import androidx.room.Room
  import androidx.room.RoomDatabase
  import android.content.Context
  import com.aivos.echovault.data.db.dao.ClipboardDao
  import com.aivos.echovault.data.db.dao.CollectionDao
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.db.entity.CollectionEntity
  import com.aivos.echovault.data.db.entity.FavoriteEntity

  @Database(
      entities = [ClipboardEntryEntity::class, FavoriteEntity::class, CollectionEntity::class],
      version = 1,
      exportSchema = false
  )
  abstract class AppDatabase : RoomDatabase() {
      abstract fun clipboardDao(): ClipboardDao
      abstract fun collectionDao(): CollectionDao
  }