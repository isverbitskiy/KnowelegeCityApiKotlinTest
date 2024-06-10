import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ResetTest : BaseApiTest() {

    @Test
    @Description("Ensures that the system resets the score and current question state for a registered user.")
    fun testResetScoreAndStateForRegisteredUser() = runBlocking {
        var response = getScore(staticEmail)
        assertCurrentScore(response)
        resetUserState(staticEmail)
        response = getScore(staticEmail)
        assertCurrentScore(response, 0)
        response = getNextQuestion(staticEmail)
        assertFullResponse(response, 0)
    }

    @Test
    @Description("Ensure that a 400 error is returned when the email parameter is missing.")
    fun testMissingEmailParameter() = runBlocking {
        val response = resetUserState("")
        assertMissingEmail(response)
    }

    @Test
    @Description("Ensures that the state is properly reset for a new user after login.")
    fun testResetStateForNewUser() = runBlocking {
        val email = generateRandomEmail()

        // Register a new user
        var response = login(email)
        assertSuccessLogged(response)

        // Verify the initial score is 0
        response = getScore(email)
        assertCurrentScore(response, 0)

        // Perform reset
        resetUserState(email)

        // Verify that the score remains 0
        response = getScore(email)
        assertCurrentScore(response, 0)

        // Verify that the first question is returned after reset
        response = getNextQuestion(email)
        assertFullResponse(response, 0)
    }

    @Test
    @Description("Try to reset state for an unauthorized user.")
    fun testResetStateForUnauthorizedUser() = runBlocking {
        val email = generateRandomEmail()
        val response = resetUserState(email)
        assertUnregisteredUser(response)
    }
}