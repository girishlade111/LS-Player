package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderItem
import com.example.data.model.SizeFilter
import com.example.data.model.SortOption
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SortFilterBottomSheet(
    currentSortOption: SortOption,
    sortAscending: Boolean,
    currentSizeFilter: SizeFilter,
    selectedFolderPath: String?,
    folders: List<FolderItem>,
    onSelectSortOption: (SortOption) -> Unit,
    onToggleSortOrder: (Boolean) -> Unit,
    onSelectSizeFilter: (SizeFilter) -> Unit,
    onSelectFolderFilter: (String?) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = Modifier.testTag("sort_filter_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort & Filter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Reset",
                    color = PrimaryIndigo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onReset() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("reset_filters_btn")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Section 1: Sort By
                SectionTitle(title = "Sort By")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SortOption.values().forEach { option ->
                        val isSelected = currentSortOption == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSortOption(option) },
                            label = { Text(option.label) },
                            leadingIcon = if (isSelected) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryIndigo,
                                selectedLeadingIconColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("sort_chip_${option.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sort Order Toggle
                SectionTitle(title = "Sort Order")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val ascText = when (currentSortOption) {
                        SortOption.NAME -> "A to Z"
                        SortOption.DATE -> "Oldest First"
                        SortOption.DURATION -> "Shortest First"
                        SortOption.SIZE -> "Smallest First"
                    }

                    val descText = when (currentSortOption) {
                        SortOption.NAME -> "Z to A"
                        SortOption.DATE -> "Newest First"
                        SortOption.DURATION -> "Longest First"
                        SortOption.SIZE -> "Largest First"
                    }

                    OrderCard(
                        title = ascText,
                        icon = Icons.Default.ArrowUpward,
                        isSelected = sortAscending,
                        onClick = { onToggleSortOrder(true) },
                        testTag = "sort_order_asc",
                        modifier = Modifier.weight(1f)
                    )

                    OrderCard(
                        title = descText,
                        icon = Icons.Default.ArrowDownward,
                        isSelected = !sortAscending,
                        onClick = { onToggleSortOrder(false) },
                        testTag = "sort_order_desc",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: File Size Filter
                SectionTitle(title = "Filter by File Size")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SizeFilter.values().forEach { filter ->
                        val isSelected = currentSizeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSizeFilter(filter) },
                            label = { Text(filter.label) },
                            leadingIcon = if (isSelected) {
                                { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo.copy(alpha = 0.15f),
                                selectedLabelColor = PrimaryIndigo,
                                selectedLeadingIconColor = PrimaryIndigo
                            ),
                            modifier = Modifier.testTag("size_filter_chip_${filter.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Folder Filter
                SectionTitle(title = "Filter by Folder")

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Option: All Folders
                    FolderFilterItem(
                        folderName = "All Folders",
                        itemCountText = "Show all videos",
                        isSelected = selectedFolderPath == null,
                        onClick = { onSelectFolderFilter(null) },
                        testTag = "folder_filter_all"
                    )

                    // Individual Folders
                    folders.forEach { folder ->
                        FolderFilterItem(
                            folderName = folder.folderName,
                            itemCountText = "${folder.videoCount} videos",
                            isSelected = selectedFolderPath == folder.folderPath,
                            onClick = { onSelectFolderFilter(folder.folderPath) },
                            testTag = "folder_filter_${folder.folderName}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Done Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("apply_sort_filter_btn")
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun OrderCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) PrimaryIndigo.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) PrimaryIndigo else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryIndigo else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FolderFilterItem(
    folderName: String,
    itemCountText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) PrimaryIndigo.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) PrimaryIndigo else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = if (isSelected) PrimaryIndigo else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folderName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = itemCountText,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
