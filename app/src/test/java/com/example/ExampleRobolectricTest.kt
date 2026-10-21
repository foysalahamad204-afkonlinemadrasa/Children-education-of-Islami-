package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FirestoreCurriculumLesson
import com.example.data.IslamicCurriculumData
import com.example.data.KidsIslamicDatabase
import com.example.data.KidsIslamicRepository
import com.example.data.StageCategory
import com.example.ui.KidsAppUiState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name and 6 stage islamic curriculum integrity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("শিশুদের ইসলাম শিক্ষা", appName)

        // Verify all 6 stages exist
        assertEquals(6, StageCategory.entries.size)

        // Verify all 29 Arabic letters are present with 4 positional forms and makhraj hints
        assertEquals(29, IslamicCurriculumData.allArabicLetters.size)
        IslamicCurriculumData.allArabicLetters.forEach { letter ->
            assertTrue(letter.arabic.isNotBlank())
            assertTrue(letter.makhrajHintBn.isNotBlank())
            assertTrue(letter.initialForm.isNotBlank())
            assertTrue(letter.medialForm.isNotBlank())
            assertTrue(letter.finalForm.isNotBlank())
        }

        // Verify Ampara surahs start with An-Naba (78) and end with An-Nas (114) in strict ascending order
        val amparaSurahs = IslamicCurriculumData.verifiedSurahLibrary.filter { it.isAmpara }
        assertTrue(amparaSurahs.isNotEmpty())
        assertEquals(78, amparaSurahs.first().surahNumber)
        assertEquals(114, amparaSurahs.last().surahNumber)
        assertEquals(amparaSurahs.map { it.surahNumber }.sorted(), amparaSurahs.map { it.surahNumber })

        // Verify Full Quran surahs include Al-Fatiha (1) and Al-Baqarah (2)
        val fullQuranSurahs = IslamicCurriculumData.verifiedSurahLibrary.filter { !it.isAmpara }
        assertTrue(fullQuranSurahs.any { it.surahNumber == 1 })
        assertTrue(fullQuranSurahs.any { it.surahNumber == 2 })
    }

    @Test
    fun `verify offline room persistence and progressive level unlocking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KidsIslamicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = KidsIslamicRepository(db.dao())

        // Initially only Level 1 is unlocked
        val initialState = KidsAppUiState()
        assertTrue(initialState.isLevelUnlocked(1))
        assertFalse(initialState.isLevelUnlocked(2))

        // Complete Level 1 offline with 3 stars
        val saved1 = repo.saveLevelCompletion(
            levelId = 1,
            starsEarned = 3,
            score = 300,
            mistakeCategory = "NONE",
            isPendingCloudSync = true
        )
        assertEquals(3, saved1.starsEarned)
        assertTrue(saved1.isPendingCloudSync)

        // Attempting to save Level 1 again with 1 star must NOT downgrade existing 3 stars
        val savedAgain = repo.saveLevelCompletion(
            levelId = 1,
            starsEarned = 1,
            score = 100,
            mistakeCategory = "HARAKAT",
            isPendingCloudSync = true
        )
        assertEquals(3, savedAgain.starsEarned)
        assertEquals(300, savedAgain.bestScore)

        val progressMap = repo.getAllLevelProgressList().associateBy { it.levelId }
        val updatedState = KidsAppUiState(levelProgressMap = progressMap)
        assertTrue(updatedState.isLevelUnlocked(2))

        db.close()
    }

    @Test
    fun `verify dynamic cloud lesson converts to playable adventure level with quiz`() {
        val cloudLesson = FirestoreCurriculumLesson(
            id = "lesson_dyn_test1",
            stageNumber = 2L,
            levelOrder = 14L,
            titleBn = "খাড়া যবর অনুশীলন",
            arabicContent = "آمَنَ",
            pronunciationBn = "আ-মানা",
            meaningBn = "ঈমান আনলো",
            audioSourceUrl = "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
            teacherVerified = true,
            isPublished = true,
            quizQuestionBn = "'آمَنَ' শব্দের অর্থ কী?",
            quizOptionsJoined = "ঈমান আনলো|পড়লো|লিখলো",
            quizCorrectIndex = 0L
        )

        val playable = cloudLesson.toPlayableAdventureLevel()
        assertEquals("খাড়া যবর অনুশীলন", playable.titleBn)
        assertEquals(StageCategory.STAGE_2_MAGIC_OF_HARAKAT, playable.category)
        assertEquals(1, playable.items.size)
        assertEquals(1, playable.challenges.size)
        assertEquals(listOf("ঈমান আনলো", "পড়লো", "লিখলো"), playable.challenges.first().options)
        assertEquals(0, playable.challenges.first().correctOptionIndex)
    }
}
