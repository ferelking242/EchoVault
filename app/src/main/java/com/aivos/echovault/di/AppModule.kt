package com.aivos.echovault.di

  import android.content.Context
  import androidx.room.Room
  import com.aivos.echovault.data.db.AppDatabase
  import com.aivos.echovault.data.db.dao.ClipboardDao
  import com.aivos.echovault.data.db.dao.CollectionDao
  import dagger.Module
  import dagger.Provides
  import dagger.hilt.InstallIn
  import dagger.hilt.android.qualifiers.ApplicationContext
  import dagger.hilt.components.SingletonComponent
  import javax.inject.Singleton

  @Module
  @InstallIn(SingletonComponent::class)
  object AppModule {

      @Provides @Singleton
      fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
          Room.databaseBuilder(context, AppDatabase::class.java, "echovault.db")
              .fallbackToDestructiveMigration()
              .build()

      @Provides @Singleton
      fun provideClipboardDao(db: AppDatabase): ClipboardDao = db.clipboardDao()

      @Provides @Singleton
      fun provideCollectionDao(db: AppDatabase): CollectionDao = db.collectionDao()
  }