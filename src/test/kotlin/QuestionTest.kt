import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class QuestionTest : BaseApiTest() {

    @Test
    @Description("Ensure that a 400 error is returned when the email parameter is missing.")
    fun testMissingEmailParameter() = runBlocking {
        val response = baseRequest {
            parameter(PARAM_ACTION, ACTION_QUESTION)
        }
        assertMissingEmail(response)
    }

    @Test
    @Description("Ensure that a 400 error is returned when the email format is invalid.")
    fun testInvalidEmailFormat() = runBlocking {
        val response = getNextQuestion("test")
        assertInvalidEmail(response)
    }

    @Test
    @Description("Verify the correct message for no more questions when all are exhausted.")
    fun testNoMoreQuestionsAvailable() = runBlocking {
        resetUserState(staticEmail)
        for (i in 0 until TOTAL_QUESTIONS) {
            getNextQuestion(staticEmail)
        }
        val response = getNextQuestion(staticEmail)
        assertNoMoreQuestions(response)
    }

    @Test
    @Description("Verifies the correct retrieval of the next question for a new user after successful login.")
    fun testNextQuestionWithNewUser() = runBlocking {
        val email = generateRandomEmail()
        login(email)
        val response = getNextQuestion(email)
        assertFullResponse(response, 0)
    }

    @Test
    @Description("Ensure that the Id field indicates a sequential order of questions for a registered user.")
    fun testQuestionOrder() = runBlocking {
        resetUserState(staticEmail)
        var previousId = -1
        var noMoreQuestions = false
        while (!noMoreQuestions) {
            val response = getNextQuestion(staticEmail).bodyAsText()
            if (response.contains(NO_MORE_QUESTIONS)) {
                noMoreQuestions = true
            } else {
                val currentId = extractQuestionId(response)
                assert(currentId > previousId) { OUT_OF_ORDER_QUESTIONS }
                previousId = currentId
            }
        }
    }

    @Test
    @Description("Verifies that the next question is returned correctly after answering the previous one successfully.")
    fun testGetNextQuestionAfterAnswer() = runBlocking {
        resetUserState(staticEmail)
        submitAnswer(staticEmail, 0, 0)
        val response = getNextQuestion(staticEmail)
        assertFullResponse(response, 1)
    }
}