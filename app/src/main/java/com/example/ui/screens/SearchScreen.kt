package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MusicCatalog
import com.example.data.SortOption
import com.example.model.Song
import com.example.ui.components.SongItemRow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OfflineEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val filter by viewModel.advancedFilter.collectAsState()
    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val currentPlayingSong by viewModel.playerManager.currentSong.collectAsState()

    var isFiltersPanelExpanded by remember { mutableStateOf(false) }

    val quickArtists = listOf("عمرو دياب", "شيرين", "نانسي عجرم", "The Weeknd", "Coldplay", "Dua Lipa", "فيروز")
    val quickAlbums = listOf("تملي معاك", "Future Nostalgia", "After Hours", "طريقي", "Evolve")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Main Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "البحث المتقدم 🔍",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "ابحث حسب الفنان، الألبوم، النوع، وسنة الإصدار",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            // Expand/Collapse Filters Button with active criteria counter badge
            BadgedBox(
                badge = {
                    val count = viewModel.activeCriteriaCount
                    if (count > 0) {
                        Badge(containerColor = NeonPink) {
                            Text("$count", color = Color.White)
                        }
                    }
                }
            ) {
                OutlinedButton(
                    onClick = { isFiltersPanelExpanded = !isFiltersPanelExpanded },
                    modifier = Modifier.testTag("toggle_filters_btn"),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFiltersPanelExpanded) DarkSurfaceVariant else DarkSurface,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "معايير التصفية",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFiltersPanelExpanded) "إخفاء المعايير" else "المعايير",
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (isFiltersPanelExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Universal Search Input
        OutlinedTextField(
            value = filter.query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("main_search_input"),
            placeholder = { Text("ابحث عن أي أغنية، كلمة، أو فنان...", color = TextTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = NeonCyan
                )
            },
            trailingIcon = {
                if (filter.query.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = DarkDivider,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Expandable Multi-Criteria Advanced Filters Card
        AnimatedVisibility(visible = isFiltersPanelExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "معايير البحث المخصصة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )

                        if (viewModel.activeCriteriaCount > 0) {
                            OutlinedButton(
                                onClick = { viewModel.resetFilters() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("reset_filters_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "إعادة ضبط",
                                    modifier = Modifier.size(16.dp),
                                    tint = NeonPink
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة ضبط", color = NeonPink, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Artist Name Filter
                    Text(
                        text = "1. اسم الفنان (Artist):",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = filter.artist,
                        onValueChange = { viewModel.updateArtistFilter(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filter_artist_input"),
                        placeholder = { Text("مثال: عمرو دياب، The Weeknd...", color = TextTertiary) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonPurple) },
                        trailingIcon = {
                            if (filter.artist.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateArtistFilter("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = DarkDivider,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    // Quick Artist suggestions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickArtists.forEach { artistName ->
                            val isSelected = filter.artist.equals(artistName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateArtistFilter(if (isSelected) "" else artistName)
                                },
                                label = { Text(artistName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Album Name Filter
                    Text(
                        text = "2. اسم الألبوم (Album):",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = filter.album,
                        onValueChange = { viewModel.updateAlbumFilter(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("filter_album_input"),
                        placeholder = { Text("مثال: تملي معاك، After Hours...", color = TextTertiary) },
                        leadingIcon = { Icon(Icons.Default.Album, contentDescription = null, tint = NeonCyan) },
                        trailingIcon = {
                            if (filter.album.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateAlbumFilter("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkDivider,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Music Genre Filter
                    Text(
                        text = "3. النوع الموسيقي (Genre):",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MusicCatalog.availableGenres.forEach { genre ->
                            val isSelected = filter.genre == genre
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateGenreFilter(genre) },
                                label = { Text(genre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Release Year Filter
                    Text(
                        text = "4. سنة الإصدار (Release Year):",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MusicCatalog.availableYears.forEach { yearStr ->
                            val isSelected = filter.yearRange == yearStr
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateYearRangeFilter(yearStr) },
                                label = { Text(yearStr, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPink,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Offline Only Toggle & Sorting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = filter.onlyOffline,
                                onCheckedChange = { viewModel.updateOnlyOfflineFilter(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OfflineEmerald,
                                    checkedTrackColor = OfflineEmerald.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("offline_only_switch")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المحملة بدون نت فقط 📥",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Sort Options
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "ترتيب",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ترتيب حسب:",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SortOption.values().forEach { sortOpt ->
                            val isSelected = filter.sortOption == sortOpt
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSortOption(sortOpt) },
                                label = { Text(sortOpt.titleArabic, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkSurfaceElevated,
                                    selectedLabelColor = NeonCyan,
                                    containerColor = DarkSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Active Criteria Summary Row (when panel is collapsed)
        if (!isFiltersPanelExpanded && viewModel.activeCriteriaCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (filter.artist.isNotBlank()) {
                    ActiveFilterBadge(
                        text = "فنان: ${filter.artist}",
                        onClear = { viewModel.updateArtistFilter("") }
                    )
                }
                if (filter.album.isNotBlank()) {
                    ActiveFilterBadge(
                        text = "ألبوم: ${filter.album}",
                        onClear = { viewModel.updateAlbumFilter("") }
                    )
                }
                if (filter.genre != "الكل (All)") {
                    ActiveFilterBadge(
                        text = "نوع: ${filter.genre}",
                        onClear = { viewModel.updateGenreFilter("الكل (All)") }
                    )
                }
                if (filter.yearRange != "جميع السنوات") {
                    ActiveFilterBadge(
                        text = "سنة: ${filter.yearRange}",
                        onClear = { viewModel.updateYearRangeFilter("جميع السنوات") }
                    )
                }
                if (filter.onlyOffline) {
                    ActiveFilterBadge(
                        text = "بدون نت فقط",
                        onClear = { viewModel.updateOnlyOfflineFilter(false) }
                    )
                }

                Text(
                    text = "مسح الكل",
                    color = NeonPink,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.resetFilters() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        // Results Header with Match Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نتائج البحث (${filteredSongs.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = filter.sortOption.titleArabic,
                style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
            )
        }

        // Results List or Empty State
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لم يتم العثور على أغانٍ تطابق معايير البحث",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "جرب تخفيف شروط البحث أو التحقق من اسم الفنان أو الألبوم أو سنة الإصدار.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetFilters() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة تعيين كافة المعايير", color = NeonCyan)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("search_results_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSongs, key = { it.id }) { song ->
                    val isCurrent = currentPlayingSong?.id == song.id
                    SongItemRow(
                        song = song,
                        isCurrentPlaying = isCurrent,
                        onPlayClick = {
                            viewModel.playSong(song, filteredSongs, isYouTube = false)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onToggleDownload = { viewModel.toggleDownload(song) },
                        onAddToPlaylistClick = { viewModel.promptAddToPlaylist(song) },
                        onShareClick = {
                            // Quick share
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp)) // Clearance for MiniPlayer
                }
            }
        }
    }
}

@Composable
fun ActiveFilterBadge(
    text: String,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "إزالة المعيار",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
