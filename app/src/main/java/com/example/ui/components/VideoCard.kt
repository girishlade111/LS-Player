package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.VideoItem
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridMode: Boolean = false
) {
    val context = LocalContext.current
    val progressFraction = if (video.durationMs > 0) {
        (video.lastPlayedPositionMs.toFloat() / video.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    if (isGridMode) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("video_grid_card_${video.id}")
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFF1E293B))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.uriString)
                            .videoFrameMillis(1000)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Format Badge
                    Surface(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.TopStart),
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.fileExtension,
                            color = PrimaryIndigo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Duration Badge
                    Surface(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomEnd),
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = video.durationFormatted,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (progressFraction > 0f) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.BottomCenter),
                            color = PrimaryIndigo,
                            trackColor = Color.Transparent
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${video.sizeFormatted} • ${video.resolutionFormatted}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("video_more_btn_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("video_list_item_${video.id}")
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.uriString)
                            .videoFrameMillis(1000)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.BottomEnd),
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = video.durationFormatted,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (progressFraction > 0f) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.BottomCenter),
                            color = PrimaryIndigo,
                            trackColor = Color.Transparent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PrimaryIndigo.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = video.fileExtension,
                                color = PrimaryIndigo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${video.sizeFormatted}  •  ${video.resolutionFormatted}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("favorite_toggle_${video.id}")
                ) {
                    Icon(
                        imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (video.isFavorite) FavoriteRed else TextMuted
                    )
                }

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("video_more_btn_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
