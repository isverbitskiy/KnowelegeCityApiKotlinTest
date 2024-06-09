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
        val QUESTIONS = arrayOf(
            "Why did the QA engineer go to the bar?",
            "How many QA engineers does it take to change a light bulb?",
            "Did the QA engineer enjoy their last bug hunt?",
            "Why did the QA engineer drown in the pool?",
            "Is it possible for a QA engineer to have too much coffee?"
        )
        val ANSWERS = arrayOf(
            "To test the bartender's skills",
            "42",
            "true",
            "Because they didn't receive the 'float' property!",
            "false"
        )
        val QUESTION_IDS = arrayOf("0", "1", "2", "3", "4")
        val faker = Faker()
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
        const val PARAM_EMAIL = "email"
        const val PARAM_ACTION = "action"
        const val PARAM_ANSWER = "answer"
        const val PARAM_QUESTION_ID = "question_id"
        const val ACTION_QUESTION = "question"
        const val ACTION_LOGIN = "login"
        const val ACTION_ANSWER = "answer"
        const val ACTION_RESET = "reset"
        const val ACTION_SCORE = "score"
        const val CORRECT_ANSWER = "Correct answer"
        const val INCORRECT_ANSWER = "Incorrect answer"
        const val TOTAL_QUESTIONS = 5
    }

    private val BASE_URL = "https://qa-test.kcdev.pro"
    protected val staticEmail = "niwatarou@gmail.com"
    private val client = HttpClient(CIO) {
        defaultRequest {
            url(BASE_URL)
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
        }
    }

    protected suspend fun baseRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return client.request {
            method = HttpMethod.Post
            block()
        }
    }

    protected fun generateRandomEmail(): String {
        val email = faker.funnyName().name().replace("\\s+".toRegex(), "")
            .lowercase(Locale.getDefault()) + "@" + faker.internet()
            .domainName()
        Allure.step("Email: $email")
        return email
    }

    protected fun generateRandomDomain(): String {
        val domain = faker.funnyName().name().replace("\\s+".toRegex(), "").lowercase(Locale.getDefault()) + ".com"
        Allure.step("Domain: $domain")
        return domain
    }

    private suspend fun allureStep(response: HttpResponse, title: String = "") {
        Allure.step("--------------------------")
        if (title.isNotEmpty()) {
            Allure.step("***** $title *****")
        }
        Allure.step("Status: ${response.status}")
        Allure.step("Body: ${response.bodyAsText()}")
        Allure.step("--------------------------")
    }

    protected suspend fun assertInvalidEmail(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(INVALID_EMAIL))
    }

    protected suspend fun assertFullResponse(response: HttpResponse, number: Int) {
        allureStep(response)
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(QUESTIONS[number]))
        assertTrue(response.bodyAsText().contains(QUESTION_IDS[number]))
        assertTrue(response.bodyAsText().contains(ANSWERS[number]))
    }

    protected suspend fun assertMissingEmail(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(MISSING_EMAIL))
    }

    protected suspend fun assertNoMoreQuestions(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(NO_MORE_QUESTIONS, response.bodyAsText())
    }

    protected suspend fun assertAlreadyLogged(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ALREADY_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertSuccessLogged(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(SUCCESS_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertMissingAction(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_ACTION, response.bodyAsText())
    }

    protected suspend fun assertInvalidAction(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(INVALID_ACTION, response.bodyAsText())
    }

    protected suspend fun assertCorrectAnswer(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(CORRECT_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertMissingQuestionId(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_QUESTION_ID, response.bodyAsText())
    }

    protected suspend fun assertMissingAnswer(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertIncorrectAnswer(response: HttpResponse) {
        allureStep(response)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(INCORRECT_ANSWER, response.bodyAsText())
    }

    protected suspend fun assertOutOfBoundsQuestion(response: HttpResponse, questionId: Int) {
        allureStep(response)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Index $questionId out of bounds for length $TOTAL_QUESTIONS", response.bodyAsText())
    }

    protected fun resetUserState(email: String) = runBlocking {
        baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_RESET)
        }.apply {
            check(status == HttpStatusCode.OK) {
                "Failed to reset user state"
            }
        }
    }

    protected suspend fun extractScore(email: String): Int {
        val response = getScore(email)
        val scorePrefix = "Current score: "
        val index = response.bodyAsText().indexOf(scorePrefix)
        return if (index != -1) {
            response.bodyAsText().substring(index + scorePrefix.length).trim().toInt()
        } else {
            throw IllegalStateException("Score not found in response")
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

    protected suspend fun getScore(email: String): HttpResponse {
        val response = baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_SCORE)
        }
        allureStep(response, "getScore():")
        return response
    }
}