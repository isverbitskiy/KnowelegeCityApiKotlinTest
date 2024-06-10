import com.github.javafaker.Faker
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.qameta.allure.Allure
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class BaseApiTest {
    companion object {
        // All questions array
        val QUESTIONS = listOf(
            "Why did the QA engineer go to the bar?",
            "How many QA engineers does it take to change a light bulb?",
            "Did the QA engineer enjoy their last bug hunt?",
            "Why did the QA engineer drown in the pool?",
            "Is it possible for a QA engineer to have too much coffee?"
        )

        // All answers array
        val ANSWERS = listOf(
            "To test the bartender's skills",
            "42",
            "true",
            "Because they didn't receive the 'float' property!",
            "false"
        )

        // Questions ID's array
        val QUESTION_IDS = listOf("0", "1", "2", "3", "4")

        // Faker instance for generating random data
        val faker = Faker()

        // Response messages
        const val INVALID_EMAIL = "Error: Invalid email address"
        const val INVALID_ACTION = "Error: Invalid action"
        const val ALREADY_LOGGED = "Error: User is already logged in"
        const val SUCCESS_LOGGED = "You have successfully logged in"
        const val MISSING_EMAIL = "Error: Email parameter is missing"
        const val MISSING_ACTION = "Error: Action parameter is missing"
        const val MISSING_QUESTION_ID = "Error: Question ID parameter is missing"
        const val MISSING_ANSWER = "Error: Answer parameter is missing"
        const val NO_MORE_QUESTIONS = "No more questions available"
        const val OUT_OF_ORDER_QUESTIONS = "Questions are out of order!"
        const val CORRECT_ANSWER = "Correct answer"
        const val INCORRECT_ANSWER = "Incorrect answer"
        const val CURRENT_SCORE_PREF = "Current score: "

        // Parameter and action names
        const val PARAM_EMAIL = "email"
        const val PARAM_ACTION = "action"
        const val PARAM_ANSWER = "answer"
        const val PARAM_QUESTION_ID = "question_id"
        const val ACTION_QUESTION = "question"
        const val ACTION_LOGIN = "login"
        const val ACTION_ANSWER = "answer"
        const val ACTION_RESET = "reset"
        const val ACTION_SCORE = "score"

        // Total number of questions
        const val TOTAL_QUESTIONS = 5
    }

    // Base URL for the API
    private val BASE_URL = "https://qa-test.kcdev.pro"

    // Static email for testing
    protected val staticEmail = "richmann@goyette.com"

    // HttpClient configuration using CIO engine
    private val client = HttpClient(CIO) {
        defaultRequest {
            url(BASE_URL)
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
        }
    }

    /**
     * Executes a base HTTP request with the given configuration block.
     *
     * @param block (HttpRequestBuilder.() -> Unit) The configuration block for the HTTP request.
     * @return (HttpResponse) The HTTP response.
     */
    protected suspend fun baseRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return client.request {
            method = HttpMethod.Post
            block()
        }
    }

    /**
     * Generates a random email address using the Faker library.
     *
     * @return (String) A random email address.
     */
    protected fun generateRandomEmail(): String {
        val email = faker.funnyName().name().replace("\\s+".toRegex(), "")
            .lowercase(Locale.getDefault()) + "@" + faker.internet()
            .domainName()
        Allure.step("Email: $email")
        return email
    }

    /**
     * Generates a random domain name using the Faker library.
     *
     * @return (String) A random domain name.
     */
    protected fun generateRandomDomain(): String {
        val domain = faker.funnyName().name().replace("\\s+".toRegex(), "").lowercase(Locale.getDefault()) + ".com"
        Allure.step("Domain: $domain")
        return domain
    }

    /**
     * Logs the details of an HTTP response to Allure.
     *
     * @param response (HttpResponse) The HTTP response to be logged.
     * @param title    (String) An optional title for the log entry. If not provided, the log will include only the response status and body.
     */
    private suspend fun allureStep(response: HttpResponse, title: String = "") {
        if (title.isNotEmpty()) {
            Allure.step("***** $title ")
        }
        Allure.step("Status: ${response.status}")
        Allure.step("Body: ${response.bodyAsText()} *****")
    }

    /**
     * Asserts that the response matches the expected status and optionally the expected body.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @param expectedStatus (HttpStatusCode) The expected HTTP status code.
     * @param stepName (String) The name of the step for Allure reporting.
     * @param expectedBody (String?) The expected body content, or null if not applicable.
     *                      If a number within the range of question IDs, it checks that the response
     *                      contains the corresponding question, question ID, and answer.
     * @throws AssertionError if the response does not match the expected status or body.
     */
    private suspend fun assertResponse(
        response: HttpResponse,
        expectedStatus: HttpStatusCode,
        stepName: String,
        expectedBody: String? = null
    ) {
        allureStep(response, stepName)
        assertEquals(expectedStatus, response.status)
        expectedBody?.let {
            if (it.toIntOrNull() != null) {
                val num = it.toInt()
                if (num in 0 until TOTAL_QUESTIONS) {
                    assertTrue(response.bodyAsText().contains(QUESTIONS[num]))
                    assertTrue(response.bodyAsText().contains(QUESTION_IDS[num]))
                    assertTrue(response.bodyAsText().contains(ANSWERS[num]))
                } else {
                    assertEquals(it, response.bodyAsText())
                }
            } else {
                assertEquals(it, response.bodyAsText())
            }
        }
    }

    /**
     * Asserts that the response indicates an invalid email error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected invalid email message.
     */
    protected suspend fun assertInvalidEmail(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertInvalidEmail()", INVALID_EMAIL)
    }

    /**
     * Asserts that the response contains the full details of a question.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @param number (Int) The index of the question, answer, and question ID to verify in the response.
     * @throws AssertionError if the response status is not OK or the body does not contain the expected question details.
     */
    protected suspend fun assertFullResponse(response: HttpResponse, number: Int) {
        assertResponse(response, HttpStatusCode.OK, "assertFullResponse()", number.toString())
    }

    /**
     * Asserts that the response indicates a missing email error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected missing email message.
     */
    protected suspend fun assertMissingEmail(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertMissingEmail()", MISSING_EMAIL)
    }

    /**
     * Asserts that the response indicates there are no more questions available.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not OK or the body does not contain the expected "no more questions" message.
     */
    protected suspend fun assertNoMoreQuestions(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.OK, "assertNoMoreQuestions()", NO_MORE_QUESTIONS)
    }

    /**
     * Asserts that the response indicates the user is already logged in.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected "already logged" message.
     */
    protected suspend fun assertAlreadyLogged(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertAlreadyLogged()", ALREADY_LOGGED)
    }

    /**
     * Asserts that the response indicates the user has successfully logged in.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not OK or the body does not contain the expected "successfully logged in" message.
     */
    protected suspend fun assertSuccessLogged(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.OK, "assertSuccessLogged()", SUCCESS_LOGGED)
    }

    /**
     * Asserts that the response indicates a missing action error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected missing action message.
     */
    protected suspend fun assertMissingAction(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertMissingAction()", MISSING_ACTION)
    }

    /**
     * Asserts that the response indicates an invalid action error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected invalid action message.
     */
    protected suspend fun assertInvalidAction(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertInvalidAction()", INVALID_ACTION)
    }

    /**
     * Asserts that the response indicates a correct answer.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not OK or the body does not contain the expected correct answer message.
     */
    protected suspend fun assertCorrectAnswer(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.OK, "assertCorrectAnswer()", CORRECT_ANSWER)
    }

    /**
     * Asserts that the response indicates a missing question ID error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected missing question ID message.
     */
    protected suspend fun assertMissingQuestionId(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertMissingQuestionId()", MISSING_QUESTION_ID)
    }

    /**
     * Asserts that the response indicates a missing answer error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected missing answer message.
     */
    protected suspend fun assertMissingAnswer(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.BadRequest, "assertMissingAnswer()", MISSING_ANSWER)
    }

    /**
     * Asserts that the response indicates an incorrect answer.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not OK or the body does not contain the expected incorrect answer message.
     */
    protected suspend fun assertIncorrectAnswer(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.OK, "assertIncorrectAnswer()", INCORRECT_ANSWER)
    }

    /**
     * Asserts that the response indicates an out of bounds question error.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @param questionId (Int) The question ID that caused the error.
     * @throws AssertionError if the response status is not BadRequest or the body does not contain the expected out of bounds message.
     */
    protected suspend fun assertOutOfBoundsQuestion(response: HttpResponse, questionId: Int) {
        assertResponse(
            response, HttpStatusCode.BadRequest, "assertOutOfBoundsQuestion()",
            "Index $questionId out of bounds for length $TOTAL_QUESTIONS"
        )
    }

    /**
     * Asserts that the response indicates the user is unregistered.
     *
     * @param response (HttpResponse) The response from the server to check.
     * @throws AssertionError if the response status is not Unauthorized.
     */
    protected suspend fun assertUnregisteredUser(response: HttpResponse) {
        assertResponse(response, HttpStatusCode.Unauthorized, "assertUnregisteredUser()")
    }

    /**
     * Asserts the current score from the given HTTP response.
     *
     * @param response (HttpResponse) The response containing the score to be verified.
     * @param expectedScore (Int) The expected score. If -1, it checks that the response contains the score prefix only.
     */
    protected suspend fun assertCurrentScore(response: HttpResponse, expectedScore: Int = -1) {
        assertResponse(response, HttpStatusCode.OK, "assertCurrentScore()")
        if (expectedScore != -1) {
            assertEquals(CURRENT_SCORE_PREF + expectedScore, response.bodyAsText())
        } else {
            assertTrue(response.bodyAsText().contains(CURRENT_SCORE_PREF))
        }
    }

    /**
     * Resets the user state with the specified email.
     *
     * @param email (String) The email of the user whose state should be reset. If not provided (empty string), the parameter is not added.
     */
    protected fun resetUserState(email: String = "") = runBlocking {
        baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_RESET)
        }
    }

    /**
     * Extracts the current score for the given email from the response.
     *
     * @param email (String) The email for which to retrieve the score.
     * @return (Int) The extracted score.
     * @throws IllegalStateException if the score is not found in the response.
     */
    protected suspend fun extractScore(email: String): Int {
        val response = getScore(email)
        allureStep(response, "extractScore()")
        val responseBody = response.bodyAsText()
        val index = responseBody.indexOf(CURRENT_SCORE_PREF)
        return if (index != -1) {
            responseBody.substring(index + CURRENT_SCORE_PREF.length).trim().toInt()
        } else {
            throw IllegalStateException("Score not found in response for email: $email")
        }
    }

    /**
     * Extracts the question ID from the given response string.
     *
     * @param response (String) The response string containing the question ID.
     * @return (Int) The extracted question ID.
     * @throws IllegalStateException if the ID is not found in the response.
     */
    protected fun extractQuestionId(response: String): Int {
        return response.lines().find {
            it.startsWith("Id:")
        }?.replace("Id:", "")?.trim()?.toInt()
            ?: throw IllegalStateException("Id not found in response")
    }

    /**
     * Executes a login request for the given email.
     *
     * @param email (String) User's email. If not provided (empty string), the parameter is not added.
     *              The action parameter is always set to "login".
     */
    protected fun login(email: String = "") = runBlocking {
        baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_LOGIN)
        }
    }

    /**
     * Retrieves the next question for the given email.
     *
     * @param email (String) User's email. If not provided (empty string), the parameter is not added.
     *              The action parameter is always set to "question".
     */
    protected fun getNextQuestion(email: String = "") = runBlocking {
        baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_QUESTION)
        }
    }

    /**
     * Submits an answer to a question with the specified parameters.
     *
     * @param email     (String) User's email. If not provided (empty string), the parameter is not added.
     * @param questionId (Int) ID of the question. If -1, the parameter is not added. If less than TOTAL_QUESTIONS, the ID from QUESTION_IDS is used.
     *                      If greater than or equal to TOTAL_QUESTIONS, the question ID value is used directly.
     * @param answer    (Int) The answer to the question. If -1, the parameter is not added. If less than TOTAL_QUESTIONS, the value from ANSWERS is used.
     *                      If greater than or equal to TOTAL_QUESTIONS, the answer value is used directly. If -2, a space character is sent.
     */
    protected suspend fun submitAnswer(email: String = "", questionId: Int = -1, answer: Int = -1) = runBlocking {
        baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_ANSWER)
            if (questionId > -1 && questionId < TOTAL_QUESTIONS) {
                parameter(PARAM_QUESTION_ID, QUESTION_IDS[questionId])
            } else if (questionId >= TOTAL_QUESTIONS) {
                parameter(PARAM_QUESTION_ID, questionId.toString())
            }
            if (answer > -1 && answer < TOTAL_QUESTIONS) {
                parameter(PARAM_ANSWER, ANSWERS[answer])
            } else if (answer >= TOTAL_QUESTIONS) {
                parameter(PARAM_ANSWER, answer)
            } else if (answer == -2) {
                parameter(PARAM_ANSWER, " ")
            }
        }
    }

    /**
     * Sends a request to get the current score of a user.
     *
     * @param email (String) User's email. If not provided (empty string), the parameter is not added.
     * @return HttpResponse The response from the server containing the user's current score.
     */
    protected suspend fun getScore(email: String = ""): HttpResponse {
        val response = baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_SCORE)
        }
        return response
    }
}