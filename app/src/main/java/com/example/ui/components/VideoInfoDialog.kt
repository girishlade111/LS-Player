package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoInfoDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(video.dateAddedMs))

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("video_info_dialog"),
        title = {
            Text(
                text = "Media Properties",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                InfoRow(label = "Title", value = video.title)
                InfoRow(label = "Format", value = video.fileExtension)
                InfoRow(label = "MIME Type", value = video.mimeType)
                InfoRow(label = "Resolution", value = video.resolutionFormatted)
                InfoRow(label = "Duration", value = video.durationFormatted)
                InfoRow(label = "File Size", value = video.sizeFormatted)
                InfoRow(label = "Date Added", value = dateString)
                InfoRow(label = "Path", value = video.path)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("video_info_close_btn")
            ) {
                Text(text = "Close", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(4.dp))
    }
}
