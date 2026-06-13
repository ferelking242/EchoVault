package com.aivos.echovault.presentation.viewmodel

  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.repository.ClipboardRepository
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.ExperimentalCoroutinesApi
  import kotlinx.coroutines.FlowPreview
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import javax.inject.Inject

  @HiltViewModel
  class SearchViewModel @Inject constructor(private val repository: ClipboardRepository) : ViewModel() {

      private val _query = MutableStateFlow("")
      val query: StateFlow<String> = _query.asStateFlow()

      @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
      val results: StateFlow<List<ClipboardEntryEntity>> = _query
          .debounce(300)
          .flatMapLatest { q ->
              if (q.isBlank()) repository.getAllEntries()
              else repository.search(q)
          }
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

      fun setQuery(q: String) { _query.value = q }
      fun deleteEntry(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.deleteEntry(entry) }
      fun toggleFavorite(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.toggleFavorite(entry) }
  }