package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrackInfo
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackBottomSheet(
    audioTracks: List<AudioTrackInfo>,
    selectedIndex: Int,
    onSelectTrack: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("audio_track_bottom_sheet"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = PrimaryIndigo
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Audio Track",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AVAILABLE AUDIO STREAMS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (audioTracks.isEmpty()) {
                Text(
                    text = "Default Audio Stream",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn {
                    itemsIndexed(audioTracks) { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTrack(index) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedIndex == index),
                                onClick = { onSelectTrack(index) },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${track.label} (${track.channelCount} ch)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
