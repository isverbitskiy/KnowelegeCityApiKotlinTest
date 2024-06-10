import io.ktor.client.request.*
import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AnswerTest : BaseApiTest() {

    @Test
    @Description("Verifies submitting the correct answer for an existing question.")
    fun testSubmitValidAnswer() = runBlocking {
        val response = submitAnswer(staticEmail, 0, 0)
        assertCorrectAnswer(response)
    }

    @Test
    @Description("Ensure that a 400 error is returned when the questionId parameter is missing.")
    fun testMissingQuestionId() = runBlocking {
        val response = submitAnswer(staticEmail, -1, 3)
        assertMissingQuestionId(response)
    }

    @Test
    @Description("Verifies handling of incorrect answer format (e.g., empty answer).")
    fun testInvalidAnswerFormat() = runBlocking {
        val response = submitAnswer(staticEmail, 1, -2)
        assertIncorrectAnswer(response)
    }

    @Test
    @Description("Verifies handling of missing answer.")
    fun testMissingAnswer() = runBlocking {
        val response = submitAnswer(staticEmail, 1)
        assertMissingAnswer(response)
    }

    @Test
    @Description("Ensure that a 400 error is returned for a non-existent question.")
    fun testNonExistingQuestionId() = runBlocking {
        val response = submitAnswer(staticEmail, TOTAL_QUESTIONS + 100, 0)
        assertOutOfBoundsQuestion(response, TOTAL_QUESTIONS + 100)
    }

    @Test
    @Description("Verifies submitting an answer to a question as a new user.")
    fun testSubmitAnswerWithNewUser() = runBlocking {
        val newEmail = generateRandomEmail()
        var response = login(newEmail)
        assertSuccessLogged(response)
        response = submitAnswer(newEmail, 1, 1)
        assertCorrectAnswer(response)
    }

    @Test
    @Description("Verifies that the system correctly interprets true/false, yes/no answers.")
    fun testSubmitBooleanAnswer() = runBlocking {
        val validAnswers = listOf("true", "yes")
        val invalidAnswers = listOf("false", "no")
        validAnswers.forEach {
            val response = baseRequest {
                parameter(PARAM_EMAIL, staticEmail)
                parameter(PARAM_ACTION, ACTION_ANSWER)
                parameter(PARAM_QUESTION_ID, QUESTION_IDS[1])
                parameter(PARAM_ANSWER, it)
            }
            assertCorrectAnswer(response)
        }
        invalidAnswers.forEach {
            val response = baseRequest {
                parameter(PARAM_EMAIL, staticEmail)
                parameter(PARAM_ACTION, ACTION_ANSWER)
                parameter(PARAM_QUESTION_ID, QUESTION_IDS[1])
                parameter(PARAM_ANSWER, it)
            }
            assertIncorrectAnswer(response)
        }
    }

    @Test
    @Description("Verifies handling of numeric answers")
    fun testSubmitNumericAnswer() = runBlocking {
        val numericAnswer = 42
        val response = submitAnswer(staticEmail, 1, numericAnswer)
        assertCorrectAnswer(response)
    }

    @Test
    @Description("Verifies that the system does not increase the score twice for the same question.")
    fun testSubmitDuplicateAnswer() = runBlocking {
        val initialScore = extractScore(staticEmail)
        submitAnswer(staticEmail, 0, 0)
        val firstAnswerScore = extractScore(staticEmail)
        assertEquals(initialScore + 1, firstAnswerScore, "Score did not increase correctly after the first answer")
        submitAnswer(staticEmail, 0, 0)
        val secondAnswerScore = extractScore(staticEmail)
        assertEquals(firstAnswerScore, secondAnswerScore, "Score increased after duplicate answer")
    }

    @Test
    @Description("Check answer for unauthorized user.")
    fun testSubmitAnswerForUnauthorizedUser() = runBlocking {
        val email = generateRandomEmail()
        val response = submitAnswer(email, 0)
        assertUnregisteredUser(response)
    }
}