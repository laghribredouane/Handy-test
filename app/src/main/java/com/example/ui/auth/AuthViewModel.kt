package com.example.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(
        if (auth.currentUser != null) AuthUiState.Authenticated(auth.currentUser!!) else AuthUiState.Idle
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            _uiState.value = AuthUiState.Authenticated(user)
        } else {
            _uiState.value = AuthUiState.Idle
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user)
                } else {
                    _uiState.value = AuthUiState.Error("Authentication failed: user is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Failed to sign in with Google")
            }
        }
    }

    fun resetError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = if (auth.currentUser != null) {
                AuthUiState.Authenticated(auth.currentUser!!)
            } else {
                AuthUiState.Idle
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
