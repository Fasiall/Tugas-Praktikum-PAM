package com.faisal.pam

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class News(
    val id: Int,
    val title: String,
    val category: String,
    val content: String = ""
)

data class DisplayNews(
    val id: Int,
    val displayTitle: String,
    val category: String
)

class NewsFeedSimulator(private val coroutineScope: CoroutineScope) {

    // 4. StateFlow untuk menyimpan jumlah berita yang sudah dibaca
    private val _readCount = MutableStateFlow(0)
    val readCount: StateFlow<Int> = _readCount.asStateFlow()

    // Sumber data kategori simulasi
    private val categories = listOf("Tech", "Sports", "Politics", "Entertainment")

    // 1. Flow yang mensimulasikan data berita baru setiap 2 detik
    // Diubah menjadi SharedFlow agar data terus berjalan (hot flow) dan menyimpan riwayat (replay)
    private val rawNewsFlow: SharedFlow<News> = flow {
        var idCounter = 1
        while (true) {
            delay(2000) // Setiap 2 detik
            val category = categories.random()
            val news = News(idCounter, "Berita Terkini $idCounter", category)
            emit(news)
            idCounter++
        }
    }.shareIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        replay = 100 // Menyimpan 100 berita terakhir agar saat filter diganti, berita lama tetap muncul
    )

    // 2. Filter berita berdasarkan kategori tertentu
    // 3. Transform data menjadi format yang ditampilkan
    fun getNewsFeed(filterCategory: String? = null): Flow<DisplayNews> {
        return rawNewsFlow
            .filter { news ->
                filterCategory == null || news.category == filterCategory
            }
            .map { news ->
                DisplayNews(
                    id = news.id,
                    displayTitle = "[${news.category}] ${news.title.uppercase()}",
                    category = news.category
                )
            }
    }

    // Fungsi untuk mensimulasikan membaca berita
    fun markAsRead() {
        _readCount.value += 1
    }

    // 5. Coroutines untuk mengambil detail berita secara async
    suspend fun fetchNewsDetail(newsId: Int): String {
        return withContext(Dispatchers.Default) {
            delay(1000) // Simulasi network delay
            "Ini adalah detail isi berita panjang untuk ID $newsId yang diambil secara asynchronous menggunakan Coroutines."
        }
    }
}
