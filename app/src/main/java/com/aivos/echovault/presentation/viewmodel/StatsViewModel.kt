package com.aivos.echovault.presentation.viewmodel

  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.repository.ClipboardRepository
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import java.text.SimpleDateFormat
  import java.util.*
  import javax.inject.Inject

  data class DayActivity(val label: String, val count: Int)

  data class StatsUiState(
      val totalEntries: Int = 0,
      val todayCount: Int = 0,
      val weekActivity: List<DayActivity> = emptyList(),
      val dbSizeBytes: Long = 0L
  )

  @HiltViewModel
  class StatsViewModel @Inject constructor(private val repository: ClipboardRepository) : ViewModel() {

      val uiState: StateFlow<StatsUiState> = combine(
          repository.getTotalCount(),
          repository.getTodayCount(),
          repository.getWeekEntries()
      ) { total, today, weekEntries ->
          val sdf = SimpleDateFormat("EEE", Locale.getDefault())
          val dayMap = mutableMapOf<String, Int>()
          val cal = Calendar.getInstance()
          for (i in 6 downTo 0) {
              cal.time = Date(); cal.add(Calendar.DAY_OF_YEAR, -i)
              val label = sdf.format(cal.time)
              dayMap[label] = 0
          }
          weekEntries.forEach { e ->
              val label = sdf.format(Date(e.timestamp))
              dayMap[label] = (dayMap[label] ?: 0) + 1
          }
          StatsUiState(
              totalEntries = total,
              todayCount = today,
              weekActivity = dayMap.entries.map { DayActivity(it.key, it.value) }
          )
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

      fun loadDbSize() = viewModelScope.launch {
          // size loaded reactively via uiState
      }
  }