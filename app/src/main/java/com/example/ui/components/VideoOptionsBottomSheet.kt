package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoOptionsBottomSheet(
    video: VideoItem,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.testTag("video_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp, top = 8.dp)
        ) {
            // Video Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.durationFormatted} • ${video.resolutionFormatted} • ${video.sizeFormatted}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Items
            OptionItem(
                icon = Icons.Default.PlayArrow,
                title = "Play Video",
                testTag = "opt_play_video",
                onClick = {
                    onPlay()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.QueuePlayNext,
                title = "Play Next in Queue",
                testTag = "opt_play_next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.Queue,
                title = "Add to Playing Queue",
                testTag = "opt_add_to_queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )

            OptionItem(
                icon = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                title = if (video.isFavorite) "Remove from Favorites" else "Add to Favorites",
                tint = if (video.isFavorite) FavoriteRed else PrimaryIndigo,
                testTag = "opt_toggle_favorite",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.Info,
                title = "Media Properties",
                testTag = "opt_video_info",
                onClick = {
                    onShowInfo()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    testTag: String,
    tint: Color = PrimaryIndigo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
