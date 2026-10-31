package com.example.data.firebase.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.firebase.models.DriveSurvey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SurveyRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeSurveys(userId: String): Flow<List<DriveSurvey>> {
        val path = "surveys"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    DriveSurvey::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt }
            }
    }

    suspend fun createSurvey(userId: String, survey: DriveSurvey): String {
        require(userId.isNotBlank()) { "User ID required for Firestore operations" }
        val docRef = db.collection("surveys").document()
        val payload = survey.copy(
            id = docRef.id,
            userId = userId,
            createdAt = null,
            updatedAt = null
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSurvey(userId: String, surveyId: String) {
        require(userId.isNotBlank()) { "User ID required" }
        require(surveyId.isNotBlank()) { "Survey ID required" }
        val docRef = db.collection("surveys").document(surveyId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
