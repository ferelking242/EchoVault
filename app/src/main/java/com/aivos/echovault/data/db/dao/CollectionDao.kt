package com.aivos.echovault.data.db.dao

  import androidx.room.*
  import com.aivos.echovault.data.db.entity.CollectionEntity
  import kotlinx.coroutines.flow.Flow

  @Dao
  interface CollectionDao {
      @Query("SELECT * FROM collections ORDER BY createdAt DESC")
      fun getAllCollections(): Flow<List<CollectionEntity>>

      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insertCollection(collection: CollectionEntity): Long

      @Delete
      suspend fun deleteCollection(collection: CollectionEntity)

      @Update
      suspend fun updateCollection(collection: CollectionEntity)
  }