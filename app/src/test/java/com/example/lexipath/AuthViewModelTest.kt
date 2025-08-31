package com.example.lexipath

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.lexipath.data.repository.LexiPathRepository
import com.example.lexipath.ui.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockRepository: LexiPathRepository
    private lateinit var mockUser: FirebaseUser
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockFirebaseAuth = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        
        every { mockUser.uid } returns "test-uid"
        every { mockUser.email } returns "test@example.com"
        
        viewModel = AuthViewModel(mockFirebaseAuth, mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be not authenticated`() = runTest {
        every { mockFirebaseAuth.currentUser } returns null
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isAuthenticated)
        assertNull(state.user)
        assertFalse(state.isLoading)
    }

    @Test
    fun `should update state when user is authenticated`() = runTest {
        every { mockFirebaseAuth.currentUser } returns mockUser
        
        viewModel.checkAuthState()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertTrue(state.isAuthenticated)
        assertEquals(mockUser, state.user)
    }

    @Test
    fun `signInWithEmail should handle success`() = runTest {
        coEvery { 
            mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) 
        } returns mockk {
            every { isSuccessful } returns true
            every { result?.user } returns mockUser
        }
        
        viewModel.signInWithEmail("test@example.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `signInWithEmail should handle failure`() = runTest {
        val exception = Exception("Sign in failed")
        coEvery { 
            mockFirebaseAuth.signInWithEmailAndPassword(any(), any()) 
        } throws exception
        
        viewModel.signInWithEmail("test@example.com", "password")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isLoading)
        assertEquals("Sign in failed", state.error)
    }

    @Test
    fun `signOut should clear user state`() = runTest {
        every { mockFirebaseAuth.currentUser } returns mockUser
        viewModel.checkAuthState()
        
        every { mockFirebaseAuth.currentUser } returns null
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isAuthenticated)
        assertNull(state.user)
    }
}
