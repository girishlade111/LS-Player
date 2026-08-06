package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.example.data.model.SizeFilter
import com.example.data.model.SortOption
import com.example.data.model.VideoItem
import com.example.ui.components.FolderCard
import com.example.ui.components.PlaylistDialog
import com.example.ui.components.RecentlyPlayedSection
import com.example.ui.components.SortFilterBottomSheet
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoInfoDialog
import com.example.ui.components.VideoOptionsBottomSheet
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.ScanState

import androidx.compose.material.icons.filled.Settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

@Composable
fun LibraryScreen(
    viewModel: MediaViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onFolderClick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onPlayNext: ((VideoItem) -> Unit)? = null,
    onAddToQueue: ((VideoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scanState by viewModel.scanState.collectAsState()
    val isGridMode by viewModel.isGridMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val sizeFilter by viewModel.sizeFilter.collectAsState()
    val selectedFolderFilter by viewModel.selectedFolderFilter.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var selectedVideoForMenu by remember { mutableStateOf<VideoItem?>(null) }
    var selectedVideoForInfo by remember { mutableStateOf<VideoItem?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.importVideo(uri)
        }
    }

    val tabs = listOf("All Videos", "Folders", "Continue", "Playlists", "Favorites")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (selectedTabIndex == 3) {
                FloatingActionButton(
                    onClick = { showCreatePlaylistDialog = true },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("create_playlist_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Playlist")
                }
            } else {
                FloatingActionButton(
                    onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("import_video_fab")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = "Import Video")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LS Player",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                    modifier = Modifier.testTag("import_video_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Import Video File",
                        tint = PrimaryIndigo
                    )
                }

                IconButton(
                    onClick = { viewModel.scanMedia() },
                    modifier = Modifier.testTag("refresh_media_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleGridMode() },
                    modifier = Modifier.testTag("toggle_layout_btn")
                ) {
                    Icon(
                        imageVector = if (isGridMode) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = TextSecondary
                    )
                }

                val isFilterActive = sizeFilter != SizeFilter.ALL || selectedFolderFilter != null
                IconButton(
                    onClick = { showSortFilterSheet = true },
                    modifier = Modifier.testTag("sort_options_btn")
                ) {
                    Icon(
                        imageVector = if (isFilterActive) Icons.Default.FilterList else Icons.Default.Sort,
                        contentDescription = "Sort & Filter",
                        tint = if (isFilterActive) PrimaryIndigo else TextSecondary
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search videos or folders...", color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Active Filter Chips Indicator
            if (sizeFilter != SizeFilter.ALL || selectedFolderFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sizeFilter != SizeFilter.ALL) {
                        AssistChip(
                            onClick = { viewModel.setSizeFilter(SizeFilter.ALL) },
                            label = { Text("Size: ${sizeFilter.label}", fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear size filter",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                labelColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("active_size_chip")
                        )
                    }

                    if (selectedFolderFilter != null) {
                        val folderName = viewModel.getFolders().find { it.folderPath == selectedFolderFilter }?.folderName ?: "Folder"
                        AssistChip(
                            onClick = { viewModel.setFolderFilter(null) },
                            label = { Text("Folder: $folderName", fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear folder filter",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                labelColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("active_folder_chip")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.resetFilters() }
                            .padding(4.dp)
                            .testTag("clear_all_filters_btn")
                    )
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = PrimaryIndigo,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryIndigo,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) PrimaryIndigo else TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Area
            when (scanState) {
                is ScanState.Scanning -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                }
                is ScanState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = (scanState as ScanState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ScanState.Success, ScanState.Idle -> {
                    when (selectedTabIndex) {
                        0 -> { // All Videos
                            val videos = viewModel.getFilteredVideos()
                            if (videos.isEmpty() && recentlyPlayed.isEmpty()) {
                                EmptyStateView(
                                    message = "No local videos found on device",
                                    subtitle = "Grant media permissions or import real video files to start watching.",
                                    onImportClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                                    onScanClick = { viewModel.scanMedia() }
                                )
                            } else if (isGridMode) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (recentlyPlayed.isNotEmpty()) {
                                        item(span = { GridItemSpan(2) }) {
                                            RecentlyPlayedSection(
                                                videos = recentlyPlayed,
                                                onVideoClick = { onVideoClick(it) },
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }

                                    if (videos.isEmpty()) {
                                        item(span = { GridItemSpan(2) }) {
                                            EmptyStateView(
                                                message = "No matching videos found",
                                                subtitle = "Try resetting your search or filters."
                                            )
                                        }
                                    } else {
                                        items(videos) { video ->
                                            VideoCard(
                                                video = video,
                                                onClick = { onVideoClick(video) },
                                                onFavoriteClick = { viewModel.toggleFavorite(video) },
                                                onMoreClick = { selectedVideoForMenu = video },
                                                isGridMode = true
                                            )
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (recentlyPlayed.isNotEmpty()) {
                                        item {
                                            RecentlyPlayedSection(
                                                videos = recentlyPlayed,
                                                onVideoClick = { onVideoClick(it) },
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }

                                    if (videos.isEmpty()) {
                                        item {
                                            EmptyStateView(
                                                message = "No matching videos found",
                                                subtitle = "Try resetting your search or filters."
                                            )
                                        }
                                    } else {
                                        items(videos) { video ->
                                            VideoCard(
                                                video = video,
                                                onClick = { onVideoClick(video) },
                                                onFavoriteClick = { viewModel.toggleFavorite(video) },
                                                onMoreClick = { selectedVideoForMenu = video },
                                                isGridMode = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        1 -> { // Folders
                            val folders = viewModel.getFolders()
                            if (folders.isEmpty()) {
                                EmptyStateView(message = "No folders found")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(folders) { folder ->
                                        FolderCard(
                                            folder = folder,
                                            onClick = { onFolderClick(folder.folderPath) }
                                        )
                                    }
                                }
                            }
                        }
                        2 -> { // Continue Watching
                            val continueVideos = viewModel.getContinueWatching()
                            if (continueVideos.isEmpty()) {
                                EmptyStateView(message = "No recently played videos")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(continueVideos) { video ->
                                        VideoCard(
                                            video = video,
                                            onClick = { onVideoClick(video) },
                                            onFavoriteClick = { viewModel.toggleFavorite(video) },
                                            onMoreClick = { selectedVideoForMenu = video }
                                        )
                                    }
                                }
                            }
                        }
                        3 -> { // Playlists
                            val playlists by viewModel.playlists.collectAsState()
                            if (playlists.isEmpty()) {
                                EmptyStateView(message = "No playlists created yet. Tap '+' to create one!")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(playlists) { pl ->
                                        FolderCard(
                                            folder = com.example.data.model.FolderItem(
                                                folderName = pl.title,
                                                folderPath = "playlist_${pl.id}",
                                                videoCount = 0,
                                                totalSizeBytes = 0L
                                            ),
                                            onClick = { }
                                        )
                                    }
                                }
                            }
                        }
                        4 -> { // Favorites
                            val favs = viewModel.getFavorites()
                            if (favs.isEmpty()) {
                                EmptyStateView(message = "No favorite videos added")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(favs) { video ->
                                        VideoCard(
                                            video = video,
                                            onClick = { onVideoClick(video) },
                                            onFavoriteClick = { viewModel.toggleFavorite(video) },
                                            onMoreClick = { selectedVideoForMenu = video }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedVideoForMenu?.let { video ->
        VideoOptionsBottomSheet(
            video = video,
            onPlay = { onVideoClick(video) },
            onPlayNext = { onPlayNext?.invoke(video) },
            onAddToQueue = { onAddToQueue?.invoke(video) },
            onToggleFavorite = { viewModel.toggleFavorite(video) },
            onShowInfo = { selectedVideoForInfo = video },
            onDismiss = { selectedVideoForMenu = null }
        )
    }

    selectedVideoForInfo?.let { video ->
        VideoInfoDialog(
            video = video,
            onDismiss = { selectedVideoForInfo = null }
        )
    }

    if (showCreatePlaylistDialog) {
        PlaylistDialog(
            onConfirm = { title ->
                viewModel.createPlaylist(title)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }

    if (showSortFilterSheet) {
        SortFilterBottomSheet(
            currentSortOption = sortOption,
            sortAscending = sortAscending,
            currentSizeFilter = sizeFilter,
            selectedFolderPath = selectedFolderFilter,
            folders = viewModel.getFolders(),
            onSelectSortOption = { viewModel.setSortOption(it) },
            onToggleSortOrder = { viewModel.setSortAscending(it) },
            onSelectSizeFilter = { viewModel.setSizeFilter(it) },
            onSelectFolderFilter = { viewModel.setFolderFilter(it) },
            onReset = { viewModel.resetFilters() },
            onDismiss = { showSortFilterSheet = false }
        )
    }
}

@Composable
private fun EmptyStateView(
    message: String,
    subtitle: String? = null,
    onImportClick: (() -> Unit)? = null,
    onScanClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (onImportClick != null || onScanClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onImportClick != null) {
                        Button(
                            onClick = onImportClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_state_import_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Video")
                        }
                    }
                    if (onScanClick != null) {
                        OutlinedButton(
                            onClick = onScanClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_state_scan_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Storage")
                        }
                    }
                }
            }
        }
    }
}
