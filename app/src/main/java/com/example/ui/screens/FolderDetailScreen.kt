package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoInfoDialog
import com.example.ui.components.VideoOptionsBottomSheet
import com.example.ui.viewmodel.MediaViewModel

@Composable
fun FolderDetailScreen(
    folderPath: String,
    viewModel: MediaViewModel,
    onBack: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onPlayNext: ((VideoItem) -> Unit)? = null,
    onAddToQueue: ((VideoItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val videos = viewModel.getVideosForFolder(folderPath)
    val folderName = if (folderPath.contains("/")) folderPath.substringAfterLast("/") else folderPath

    var selectedVideoForMenu by remember { mutableStateOf<VideoItem?>(null) }
    var selectedVideoForInfo by remember { mutableStateOf<VideoItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("folder_detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = folderName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(videos) { video ->
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
}
