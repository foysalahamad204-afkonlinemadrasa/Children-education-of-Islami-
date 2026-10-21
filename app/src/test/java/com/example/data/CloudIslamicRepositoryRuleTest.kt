package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudIslamicRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun syncSingleLessonProgress_preventsDuplicatesAndPreventsStarRegression() = runBlocking {
        val uniqueEmail = "alice_${UUID.randomUUID().toString().take(6)}@test.com"
        signInTestUser(uniqueEmail)
        val repo = CloudIslamicRepository(firestore, auth)

        // First sync: 3 stars, score 300, epoch 2000
        val firstResult = withTimeout(5000L) {
            repo.syncSingleLessonProgress(
                levelId = 1,
                localStars = 3,
                localBestScore = 300,
                localCompleted = true,
                mistakeCategory = "NONE",
                localUpdatedAtEpoch = 2000L
            )
        }
        assertTrue(firstResult.isSuccess)
        assertEquals(3L, firstResult.getOrThrow().starsEarned)

        // Second sync for the SAME lesson (levelId = 1) with older/lower offline values (1 star, score 100, epoch 1000)
        // Must NOT create a duplicate document and must NOT overwrite 3 stars with 1 star!
        val secondResult = withTimeout(5000L) {
            repo.syncSingleLessonProgress(
                levelId = 1,
                localStars = 1,
                localBestScore = 100,
                localCompleted = true,
                mistakeCategory = "HARAKAT",
                localUpdatedAtEpoch = 1000L
            )
        }
        assertTrue(secondResult.isSuccess)
        val merged = secondResult.getOrThrow()
        assertEquals("lesson_1", merged.id)
        assertEquals(3L, merged.starsEarned)
        assertEquals(300L, merged.bestScore)
    }

    @Test
    fun crossUserAccess_bobCannotReadAliceLessonProgress() = runBlocking {
        val aliceEmail = "alice_iso_${UUID.randomUUID().toString().take(6)}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val repo = CloudIslamicRepository(firestore, auth)

        withTimeout(5000L) {
            repo.syncSingleLessonProgress(
                levelId = 2,
                localStars = 2,
                localBestScore = 200,
                localCompleted = true,
                mistakeCategory = "NONE",
                localUpdatedAtEpoch = 1500L
            ).getOrThrow()
        }

        val bobEmail = "bob_iso_${UUID.randomUUID().toString().take(6)}@test.com"
        signInTestUser(bobEmail)

        try {
            withTimeout(5000L) {
                firestore.collection("users").document(aliceUid)
                    .collection("lesson_progress").document("lesson_2")
                    .get().await()
            }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's progress")
        } catch (e: Exception) {
            val fsEx = generateSequence(e as Throwable?) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertTrue("Expected PERMISSION_DENIED", fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }
    }

    @Test
    fun unauthenticatedCaller_cannotObserveProgressOrCreateLessons() = runBlocking {
        auth.signOut()
        val repo = CloudIslamicRepository(firestore, auth)

        try {
            withTimeout(3000L) {
                repo.observeUserLessonProgress("any_uid").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: Exception) {
            val fsEx = generateSequence(e as Throwable?) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertTrue("Expected PERMISSION_DENIED in cause chain", fsEx?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }

        // Non-admin user cannot create curriculum lessons
        val regularEmail = "regular_${UUID.randomUUID().toString().take(6)}@test.com"
        signInTestUser(regularEmail)
        val adminSaveAttempt = withTimeout(5000L) {
            repo.saveAdminCurriculumLesson(
                existingId = null,
                stageNumber = 1,
                levelOrder = 1,
                titleBn = "অননুমোদিত পাঠ",
                arabicContent = "ا",
                pronunciationBn = "আলিফ",
                meaningBn = "প্রথম হরফ",
                audioSourceUrl = "ar_alif",
                teacherVerified = true,
                isPublished = true,
                quizQuestionBn = "প্রথম হরফ কোনটি?",
                quizOptionsJoined = "আলিফ|বা|তা",
                quizCorrectIndex = 0
            )
        }
        assertFalse(adminSaveAttempt.isSuccess)
    }
}
