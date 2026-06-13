package com.aivos.echovault.presentation.viewmodel

  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.repository.ClipboardRepository
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.ExperimentalCoroutinesApi
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import java.util.Calendar
  import javax.inject.Inject

  enum class TimeFilter { ALL, TODAY, YESTERDAY, WEEK, MONTH }

  data class HomeUiState(
      val entries: List<ClipboardEntryEntity> = emptyList(),
      val isLoading: Boolean = true,
      val selectedFilter: TimeFilter = TimeFilter.ALL
  )

  @HiltViewModel
  class HomeViewModel @Inject constructor(private val repository: ClipboardRepository) : ViewModel() {

      private val _filter = MutableStateFlow(TimeFilter.ALL)

      @OptIn(ExperimentalCoroutinesApi::class)
      val uiState: StateFlow<HomeUiState> = _filter.flatMapLatest { filter ->
          val flow = when (filter) {
              TimeFilter.ALL -> repository.getAllEntries()
              TimeFilter.TODAY -> {
                  val start = todayStart(); repository.getEntriesByRange(start, Long.MAX_VALUE)
              }
              TimeFilter.YESTERDAY -> {
                  val end = todayStart(); val start = end - 86400000L; repository.getEntriesByRange(start, end)
              }
              TimeFilter.WEEK -> repository.getWeekEntries()
              TimeFilter.MONTH -> {
                  val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                  repository.getEntriesByRange(cal.timeInMillis, Long.MAX_VALUE)
              }
          }
          flow.map { HomeUiState(entries = it, isLoading = false, selectedFilter = filter) }
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

      fun setFilter(filter: TimeFilter) { _filter.value = filter }
      fun toggleFavorite(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.toggleFavorite(entry) }
      fun togglePin(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.togglePin(entry) }
      fun deleteEntry(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.deleteEntry(entry) }

      private fun todayStart(): Long {
          val cal = Calendar.getInstance()
          cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
          return cal.timeInMillis
      }
  }