package com.example.lexipath.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for health check and other public endpoints
        if (originalRequest.url.encodedPath.contains("healthz")) {
            return chain.proceed(originalRequest)
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        return try {
            val token = runBlocking {
                currentUser.getIdToken(false).await().token
            }
            
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
                
            chain.proceed(authenticatedRequest)
        } catch (e: Exception) {
            // If token fetch fails, proceed without auth header
            chain.proceed(originalRequest)
        }
    }
}
