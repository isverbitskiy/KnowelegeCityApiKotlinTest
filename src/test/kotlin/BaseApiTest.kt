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
     * @return (String) A random domain name.
     */
    protected fun generateRandomDomain(): String {
        val domain = faker.funnyName().name().replace("\\s+".toRegex(), "").lowercase(Locale.getDefault()) + ".com"
        Allure.step("Domain: $domain")
        return domain
    }

    /**
     * Logs the details of an HTTP response to Allure.
     * @param title (String) An optional title for the log entry.
     *  If not provided, the log will include only the response status and body.
     */
    private suspend fun allureStep(response: HttpResponse, title: String = "") {
        if (title.isNotEmpty()) {
            Allure.step("***** $title ")
        }
        Allure.step("Status: ${response.status}")
        Allure.step("Body: ${response.bodyAsText()} *****")
    }

    protected suspend fun assertInvalidEmail(response: HttpResponse) {
        allureStep(response, "assertInvalidEmail()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(INVALID_EMAIL, response.bodyAsText())
    }

    protected suspend fun assertFullResponse(response: HttpResponse, number: Int) {
        allureStep(response, "assertFullResponse()")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(QUESTIONS[number]))
        assertTrue(response.bodyAsText().contains(QUESTION_IDS[number]))
        assertTrue(response.bodyAsText().contains(ANSWERS[number]))
    }

    protected suspend fun assertMissingEmail(response: HttpResponse) {
        allureStep(response, "assertMissingEmail()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_EMAIL, response.bodyAsText())
    }

    protected suspend fun assertNoMoreQuestions(response: HttpResponse) {
        allureStep(response, "assertNoMoreQuestions()")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(NO_MORE_QUESTIONS, response.bodyAsText())
    }

    protected suspend fun assertAlreadyLogged(response: HttpResponse) {
        allureStep(response, "assertAlreadyLogged()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ALREADY_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertSuccessLogged(response: HttpResponse) {
        allureStep(response, "assertSuccessLogged()")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(SUCCESS_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertMissingAction(response: HttpResponse) {
        allureStep(response, "assertMissingAction()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_ACTION, response.bodyAsText())
    }

    protected suspend fun assertInvalidAction(response: HttpResponse) {
        allureStep(response, "assertInvalidAction()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(INVALID_ACTION, response.bodyAsText())
    }

    protected suspend fun assertCorrectAnswer(response: HttpResponse) {
        allureStep(response, "assertCorrectAnswer()")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(CORRECT_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertMissingQuestionId(response: HttpResponse) {
        allureStep(response, "assertMissingQuestionId()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_QUESTION_ID, response.bodyAsText())
    }

    protected suspend fun assertMissingAnswer(response: HttpResponse) {
        allureStep(response, "assertMissingAnswer()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertIncorrectAnswer(response: HttpResponse) {
        allureStep(response, "assertIncorrectAnswer()")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(INCORRECT_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertOutOfBoundsQuestion(response: HttpResponse, questionId: Int) {
        allureStep(response, "assertOutOfBoundsQuestion()")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Index $questionId out of bounds for length $TOTAL_QUESTIONS", response.bodyAsText())
    }

    /**
     * @param expectedScore (Int) The expected score.
     *  If -1, it checks that the response contains the score prefix only.
     */
    protected suspend fun assertCurrentScore(response: HttpResponse, expectedScore: Int = -1) {
        allureStep(response, "assertCurrentScore()")
        assertEquals(HttpStatusCode.OK, response.status)
        if (expectedScore != -1) {
            assertEquals(CURRENT_SCORE_PREF + expectedScore, response.bodyAsText())
        } else {
            assertTrue(response.bodyAsText().contains(CURRENT_SCORE_PREF))
        }
    }

    /**
     * @param email (String) The email of the user whose state should be reset.
     *  If not provided (empty string), the parameter is not added.
     */
    protected fun resetUserState(email: String = "") = runBlocking {
        baseRequest {
            if (email.isNotEmpty()) {
                parameter(PARAM_EMAIL, email)
            }
            parameter(PARAM_ACTION, ACTION_RESET)
        }
    }

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

    protected fun extractQuestionId(response: String): Int {
        return response.lines().find {
            it.startsWith("Id:")
        }?.replace("Id:", "")?.trim()?.toInt()
            ?: throw IllegalStateException("Id not found in response")
    }

    protected fun login(email: String) = runBlocking {
        baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_LOGIN)
        }
    }

    protected fun getNextQuestion(email: String) = runBlocking {
        baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_QUESTION)
        }
    }

    /**
     * @param email (String) User's email.
     *  If not provided (empty string), the parameter is not added.
     * @param questionId (Int) ID of the question.
     *  If -1, the parameter is not added.
     *  If less than TOTAL_QUESTIONS, the ID from QUESTION_IDS is used.
     *  If greater than or equal to TOTAL_QUESTIONS, the question ID value is used directly.
     * @param answer (Int) The answer to the question.
     *  If -1, the parameter is not added.
     *  If less than TOTAL_QUESTIONS, the value from ANSWERS is used.
     *  If greater than or equal to TOTAL_QUESTIONS, the answer value is used directly.
     *  If -2, a space character is sent.
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