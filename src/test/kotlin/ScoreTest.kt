import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ScoreTest : BaseApiTest() {

    @Test
    @Description("Checks that the system correctly displays the current score for the registered user.")
    fun testDisplayCurrentScoreForRegisteredUser() = runBlocking {
        val response = getScore(staticEmail)
        assertCurrentScore(response)
    }

    @Test
    @Description("Checks that a 400 error is returned when the email parameter is missing.")
    fun testMissingEmailParameter() = runBlocking {
        val response = getScore("")
        assertMissingEmail(response)
    }

    @Test
    @Description("Checks the score is displayed correctly for a new user after a successful login.")
    fun testDisplayScoreForNewUser() = runBlocking {
        val email = generateRandomEmail()
        val loginResponse = login(email)
        assertSuccessLogged(loginResponse)
        val scoreResponse = getScore(email)
        assertCurrentScore(scoreResponse, 0)
    }

    @Test
    @Description("Checks that the score is reset after calling reset.")
    fun testResetScore() = runBlocking {
        resetUserState(staticEmail)
        val response = getScore(staticEmail)
        assertCurrentScore(response, 0)
    }

    @Test
    @Description("Checks that the score is displayed correctly after successfully answering several questions.")
    fun testDisplayScoreAfterAnsweringQuestions() = runBlocking {
        val currentScore = extractScore(staticEmail)
        var response = submitAnswer(staticEmail, 0, 0)
        assertCorrectAnswer(response)
        response = submitAnswer(staticEmail, 1, 1)
        assertCorrectAnswer(response)
        response = getScore(staticEmail)
        assertCurrentScore(response, currentScore + 2)
    }

    @Test
    @Description("Checks that the score cannot be displayed correctly for an unauthorized user.")
    fun testCheckScoreForUnauthorizedUser() = runBlocking {
        val email = generateRandomEmail()
        val response = getScore(email)
        assertUnregisteredUser(response)
    }
}