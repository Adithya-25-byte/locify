package com.example.Locify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.Locify.data.FavoriteLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onResultSelected: (SearchResult) -> Unit,
    searchResults: List<SearchResult>,
    favoriteLocations: List<FavoriteLocation> = emptyList(),
    onFavoriteSelected: (FavoriteLocation) -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }

    Column {
        SearchBar(
            query = query,
            onQueryChange = {
                onQueryChange(it)
                showDropdown = it.isNotEmpty()
            },
            onSearch = {
                onSearch(it)
                showDropdown = true
            },
            active = showDropdown,
            onActiveChange = { showDropdown = it },
            placeholder = { Text("Search for a location") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (favoriteLocations.isNotEmpty()) {
                Text(
                    "Favorites",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(favoriteLocations) { favorite ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFavoriteSelected(favorite) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = Color(0xFFCA96E7), // Light purple color
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(
                                    text = favorite.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = favorite.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            if (searchResults.isNotEmpty()) {
                Text(
                    "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(searchResults) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onResultSelected(result)
                                    showDropdown = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Result",
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(
                                    text = result.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = result.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}