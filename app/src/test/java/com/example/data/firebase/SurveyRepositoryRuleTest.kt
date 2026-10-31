package com.example.data.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.models.DriveSurvey
import com.example.data.firebase.repository.SurveyRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SurveyRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUser_cannotCreateSurvey() = runBlocking {
    val repository = SurveyRepository(firestore)
    auth.signOut()

    val survey = DriveSurvey(
      name = "Unauthenticated Survey",
      networkType = "LTE",
      operator = "Test"
    )

    try {
      repository.createSurvey("unauth_uid", survey)
      fail("Expected exception when unauthenticated user writes survey")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException but got $e", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  @Test
  fun authenticatedUser_canCreateAndReadSurvey() = runBlocking {
    val repository = SurveyRepository(firestore)
    val aliceUid = signInTestUser("alice@example.com")

    val survey = DriveSurvey(
      name = "Downtown 5G Drive",
      networkType = "5G",
      operator = "Carrier X",
      avgRsrp = -82.0,
      avgSinr = 21.5,
      pci = 104,
      band = "n78",
      pointCount = 85,
      status = "completed"
    )

    val docId = repository.createSurvey(aliceUid, survey)
    assertTrue("Survey document ID should be generated", docId.isNotBlank())

    val surveys = repository.observeSurveys(aliceUid).first()
    assertTrue("Should retrieve created survey for Alice", surveys.any { it.name == "Downtown 5G Drive" })
  }

  @Test
  fun authenticatedUser_cannotReadAnotherUsersSurvey() = runBlocking {
    val repository = SurveyRepository(firestore)
    val aliceUid = signInTestUser("alice_survey_owner@example.com")

    val survey = DriveSurvey(
      name = "Alice Private Drive",
      networkType = "LTE",
      operator = "Carrier Y"
    )
    val docId = repository.createSurvey(aliceUid, survey)
    assertTrue(docId.isNotBlank())

    // Sign in as Bob
    val bobUid = signInTestUser("bob_intruder@example.com")

    // Bob observing Alice's surveys path directly should return empty or fail due to rules
    val bobSurveys = repository.observeSurveys(bobUid).first()
    assertTrue("Bob should not see Alice's surveys", bobSurveys.none { it.name == "Alice Private Drive" })
  }
}
