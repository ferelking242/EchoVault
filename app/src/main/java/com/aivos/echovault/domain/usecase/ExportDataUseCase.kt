package com.aivos.echovault.domain.usecase

  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.repository.ClipboardRepository
  import com.google.gson.GsonBuilder
  import kotlinx.coroutines.flow.first
  import java.text.SimpleDateFormat
  import java.util.*
  import javax.inject.Inject

  class ExportDataUseCase @Inject constructor(private val repository: ClipboardRepository) {

      suspend fun exportAsTxt(): String {
          val entries = repository.getAllEntries().first()
          val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
          return buildString {
              appendLine("EchoVault Export" + " -- " + sdf.format(Date()))
              appendLine("Total entries: " + entries.size)
              appendLine("=".repeat(60))
              entries.forEach { e ->
                  appendLine("[" + sdf.format(Date(e.timestamp)) + "] [" + e.contentType + "]")
                  appendLine(e.content)
                  appendLine("-".repeat(40))
              }
          }
      }

      suspend fun exportAsJson(): String {
          val entries = repository.getAllEntries().first()
          return GsonBuilder().setPrettyPrinting().create().toJson(entries)
      }

      suspend fun exportAsCsv(): String {
          val entries = repository.getAllEntries().first()
          val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
          return buildString {
              appendLine("id,timestamp,contentType,charCount,isFavorite,content")
              entries.forEach { e ->
                  val safe = e.content.replace("\n", " ").take(500)
                  appendLine(e.id.toString() + "," + sdf.format(Date(e.timestamp)) + "," + e.contentType + "," + e.charCount + "," + e.isFavorite + "," + safe)
              }
          }
      }

      suspend fun importFromJson(json: String): Int {
          val entries = GsonBuilder().create().fromJson(json, Array<ClipboardEntryEntity>::class.java)
          var count = 0
          entries.forEach { e -> repository.addEntry(e.content); count++ }
          return count
      }
  }