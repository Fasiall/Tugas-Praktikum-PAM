package com.faisal.pam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        val simulator = remember { NewsFeedSimulator(coroutineScope) }

        val readCount by simulator.readCount.collectAsState()
        val newsList = remember { mutableStateListOf<DisplayNews>() }
        var selectedCategory by remember { mutableStateOf<String?>(null) }
        var selectedNewsDetail by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(selectedCategory) {
            newsList.clear()
            simulator.getNewsFeed(selectedCategory).collect { news ->
                newsList.add(0, news)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "News Feeds Simulator",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total Berita Dibaca: $readCount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(null, "Tech", "Sports", "Politics", "Entertainment").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category ?: "All") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedNewsDetail != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ringkasan Detail:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(selectedNewsDetail!!)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { selectedNewsDetail = null }) {
                            Text("Tutup")
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(newsList, key = { it.id }) { news ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                coroutineScope.launch {
                                    selectedNewsDetail = "Memuat..."
                                    simulator.markAsRead()
                                    val detail = simulator.fetchNewsDetail(news.id)
                                    selectedNewsDetail = detail
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = news.displayTitle, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Kategori: ${news.category}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}