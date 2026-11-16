package com.ks.tvlauncher.ui.apps

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.apps.AppsRepository
import com.ks.tvlauncher.data.apps.PinnedAppsRepository
import com.ks.tvlauncher.domain.AppCategory
import com.ks.tvlauncher.domain.AppFilter
import com.ks.tvlauncher.domain.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.Collator

@Immutable
data class DrawerApp(val app: AppInfo, val pinned: Boolean)

@Immutable
data class AppsDrawerUiState(
    val loaded: Boolean = false,
    /** The apps that match the search and filter, in the chosen order. */
    val apps: List<DrawerApp> = emptyList(),
    val installedCount: Int = 0,
    val pinnedCount: Int = 0,
    val query: String = "",
    /** Only categories that have at least one app. */
    val categories: List<AppCategory> = emptyList(),
    /** Null is "All". */
    val category: AppCategory? = null,
    val ascending: Boolean = true,
    /** While on, OK on a tile opens its menu instead of the app. */
    val editMode: Boolean = false,
)

/** Search, filter and sort state for the All apps drawer. Pinning and launching live in Home. */
class AppsDrawerViewModel(
    appsRepository: AppsRepository,
    pinnedAppsRepository: PinnedAppsRepository,
) : ViewModel() {

    private data class Filters(
        val query: String = "",
        val category: AppCategory? = null,
        val ascending: Boolean = true,
        val editMode: Boolean = false,
    )

    private val filters = MutableStateFlow(Filters())

    val uiState: StateFlow<AppsDrawerUiState> =
        combine(appsRepository.apps, pinnedAppsRepository.pinned, filters) { apps, pinned, f ->
            if (apps == null) return@combine AppsDrawerUiState(query = f.query, ascending = f.ascending, editMode = f.editMode)
            val pinnedSet = pinned.orEmpty().toSet()
            val categories = AppFilter.availableCategories(apps)
            // A category that has just lost its last app falls back to All.
            val category = f.category?.takeIf { it in categories }
            val collator = Collator.getInstance()
            val visible = AppFilter.apply(apps, f.query, category, f.ascending) { a, b -> collator.compare(a, b) }
            AppsDrawerUiState(
                loaded = true,
                apps = visible.map { DrawerApp(it, it.packageName in pinnedSet) },
                installedCount = apps.size,
                pinnedCount = apps.count { it.packageName in pinnedSet },
                query = f.query,
                categories = categories,
                category = category,
                ascending = f.ascending,
                editMode = f.editMode,
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsDrawerUiState())

    fun setQuery(query: String) = filters.update { it.copy(query = query) }

    fun setCategory(category: AppCategory?) = filters.update { it.copy(category = category) }

    fun toggleSort() = filters.update { it.copy(ascending = !it.ascending) }

    fun toggleEditMode() = filters.update { it.copy(editMode = !it.editMode) }

    /** Called when the drawer closes: the next visit starts with no search and Edit off. */
    fun reset() = filters.update { it.copy(query = "", editMode = false) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as KsApplication).container
                AppsDrawerViewModel(container.appsRepository, container.pinnedAppsRepository)
            }
        }
    }
}
