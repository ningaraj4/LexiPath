package com.example.lexipath

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.lexipath.data.local.dao.DailyContentDao
import com.example.lexipath.data.local.dao.ProfileDao
import com.example.lexipath.data.local.entities.DailyContentEntity
import com.example.lexipath.data.models.*
import com.example.lexipath.data.remote.ApiService
import com.example.lexipath.data.repository.LexiPathRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LexiPathRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var mockApiService: ApiService
    private lateinit var mockDailyContentDao: DailyContentDao
    private lateinit var mockProfileDao: ProfileDao
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var repository: LexiPathRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockApiService = mockk(relaxed = true)
        mockDailyContentDao = mockk(relaxed = true)
        mockProfileDao = mockk(relaxed = true)
        mockFirebaseAuth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        
        every { mockUser.uid } returns "test-uid"
        every { mockFirebaseAuth.currentUser } returns mockUser
        
        repository = LexiPathRepository(
            apiService = mockApiService,
            dailyContentDao = mockDailyContentDao,
            profileDao = mockProfileDao,
            firebaseAuth = mockFirebaseAuth
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getTodaysContent should return cached content when available`() = runTest {
        val today = LocalDate.now().toString()
        val cachedContent = DailyContentEntity(
            id = "test-id",
            word = "test",
            meaning = "test meaning",
            date = today,
            examplesTarget = listOf("example"),
            examplesNative = listOf("example"),
            pronunciation = "test"
        )
        
        every { mockDailyContentDao.getContentByDate(today) } returns flowOf(cachedContent)
        
        val result = repository.getTodaysContent(today)
        
        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.word)
    }

    @Test
    fun `getTodaysContent should fetch from API when cache is empty`() = runTest {
        val today = LocalDate.now().toString()
        val apiContent = DailyContent(
            id = "api-id",
            word = "api-word",
            meaning = "api meaning",
            date = today,
            examplesTarget = listOf("api example"),
            examplesNative = listOf("api example"),
            pronunciation = "api"
        )
        
        every { mockDailyContentDao.getContentByDate(today) } returns flowOf(null)
        coEvery { mockApiService.getTodaysContent(today) } returns Response.success(apiContent)
        coEvery { mockDailyContentDao.insertContent(any()) } just Runs
        
        val result = repository.getTodaysContent(today)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(result.isSuccess)
        assertEquals("api-word", result.getOrNull()?.word)
        coVerify { mockDailyContentDao.insertContent(any()) }
    }

    @Test
    fun `submitProfile should call API and cache locally`() = runTest {
        val profileRequest = ProfileRequest(
            goalType = GoalType.bilingual_language_track,
            languageLevel = LanguageLevel.intermediate,
            targetLanguage = "Spanish",
            nativeLanguage = "English",
            industry = null
        )
        
        val profileResponse = Profile(
            id = "profile-id",
            userId = "test-uid",
            goalType = GoalType.bilingual_language_track,
            languageLevel = LanguageLevel.intermediate,
            targetLanguage = "Spanish",
            nativeLanguage = "English",
            industry = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        coEvery { mockApiService.submitProfile(profileRequest) } returns Response.success(profileResponse)
        coEvery { mockProfileDao.insertProfile(any()) } just Runs
        
        val result = repository.submitProfile(profileRequest)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(result.isSuccess)
        assertEquals("profile-id", result.getOrNull()?.id)
        coVerify { mockProfileDao.insertProfile(any()) }
    }

    @Test
    fun `submitQuiz should handle API call correctly`() = runTest {
        val quizRequest = QuizRequest(
            contentId = "content-id",
            type = QuizType.mcq,
            userAnswer = "correct answer",
            isCorrect = true,
            timeSpentMs = 5000
        )
        
        val quizResponse = QuizResponse(
            isCorrect = true,
            correctAnswer = "correct answer",
            explanation = "Good job!",
            masteryUpdate = MasteryUpdate(
                wordId = "word-id",
                oldScore = 50,
                newScore = 60,
                change = 10
            )
        )
        
        coEvery { mockApiService.submitQuiz(quizRequest) } returns Response.success(quizResponse)
        
        val result = repository.submitQuiz(quizRequest)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isCorrect == true)
    }
}
