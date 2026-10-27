package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AdvancedSearchFilter
import com.example.data.MusicCatalog
import com.example.data.MusicRepository
import com.example.data.SortOption
import com.example.data.local.MusicDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var repository: MusicRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val db = MusicDatabase.getDatabase(context)
        repository = MusicRepository(db.musicDao())
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Have Fun Music", appName)
    }

    @Test
    fun testCatalogHasSampleSongs() {
        assertTrue(MusicCatalog.sampleSongs.isNotEmpty())
        val amrDiab = MusicCatalog.sampleSongs.find { it.artist.contains("عمرو دياب") }
        assertNotNull(amrDiab)
        assertEquals(2000, amrDiab?.releaseYear)
    }

    @Test
    fun testAdvancedSearchByArtist() {
        val songs = MusicCatalog.sampleSongs
        val filter = AdvancedSearchFilter(artist = "The Weeknd")
        val results = repository.filterSongs(songs, filter)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.artist.contains("The Weeknd", ignoreCase = true) })
    }

    @Test
    fun testAdvancedSearchByAlbum() {
        val songs = MusicCatalog.sampleSongs
        val filter = AdvancedSearchFilter(album = "Future Nostalgia")
        val results = repository.filterSongs(songs, filter)

        assertEquals(1, results.size)
        assertEquals("Levitating", results[0].title)
    }

    @Test
    fun testAdvancedSearchByGenre() {
        val songs = MusicCatalog.sampleSongs
        val filter = AdvancedSearchFilter(genre = "Arabic Pop")
        val results = repository.filterSongs(songs, filter)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.genre.contains("Arabic Pop", ignoreCase = true) })
    }

    @Test
    fun testAdvancedSearchByReleaseYear() {
        val songs = MusicCatalog.sampleSongs
        // Year 2024
        val filter2024 = AdvancedSearchFilter(yearRange = "2024")
        val results2024 = repository.filterSongs(songs, filter2024)
        assertTrue(results2024.isNotEmpty())
        assertTrue(results2024.all { it.releaseYear == 2024 })

        // Classics (before 2000)
        val filterClassics = AdvancedSearchFilter(yearRange = "قبل 2000 (كلاسيكيات)")
        val resultsClassics = repository.filterSongs(songs, filterClassics)
        assertTrue(resultsClassics.isNotEmpty())
        assertTrue(resultsClassics.all { it.releaseYear < 2000 })
    }

    @Test
    fun testAdvancedSearchMultiCriteria() {
        val songs = MusicCatalog.sampleSongs
        // Search by Artist + Genre
        val filter = AdvancedSearchFilter(
            artist = "Coldplay",
            genre = "Alternative Rock"
        )
        val results = repository.filterSongs(songs, filter)

        assertEquals(1, results.size)
        assertEquals("Viva La Vida", results[0].title)
    }

    @Test
    fun testOfflineFilter() {
        val songs = MusicCatalog.sampleSongs
        val filter = AdvancedSearchFilter(onlyOffline = true)
        val results = repository.filterSongs(songs, filter)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.isOfflineDownloaded })
    }

    @Test
    fun testCreateAndImportPlaylist() = runBlocking {
        val songs = MusicCatalog.sampleSongs
        val pl = repository.createPlaylist("قائمة السفر", "أغاني للرحلات", "#FF2A85")
        assertNotNull(pl)
        assertTrue(pl.shareCode.startsWith("HFM-"))

        val imported = repository.importPlaylistFromShareCode(pl.shareCode, songs)
        assertNotNull(imported)
    }
}
