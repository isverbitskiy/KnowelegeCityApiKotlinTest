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
        const val MISSING_EMAIL = "Error: Email parameter is missing"
        const val ALREADY_LOGGED = "Error: User is already logged in"
        const val SUCCESS_LOGGED = "You have successfully logged in"
        const val MISSING_ACTION = "Error: Action parameter is missing"
        const val INVALID_ACTION = "Error: Invalid action"
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

    protected suspend fun assertInvalidEmail(response: HttpResponse) {
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(INVALID_EMAIL))
    }

    protected suspend fun assertFullResponse(response: HttpResponse, number: Int) {
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(QUESTIONS[number]))
        assertTrue(response.bodyAsText().contains(QUESTION_IDS[number]))
        assertTrue(response.bodyAsText().contains(ANSWERS[number]))
    }

    protected suspend fun assertMissingEmail(response: HttpResponse) {
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(MISSING_EMAIL))
    }

    protected suspend fun assertNoMoreQuestions(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(NO_MORE_QUESTIONS, response.bodyAsText())
    }

    protected suspend fun assertAlreadyLogged(response: HttpResponse) {
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ALREADY_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertSuccessLogged(response: HttpResponse) {
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(SUCCESS_LOGGED, response.bodyAsText())
    }

    protected suspend fun assertMissingAction(response: HttpResponse) {
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(MISSING_ACTION, response.bodyAsText())
    }

    protected suspend fun assertInvalidAction(response: HttpResponse) {
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(INVALID_ACTION, response.bodyAsText())
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

    protected fun extractScore(response: String): Int {
        val scorePrefix = "Current score: "
        val index = response.indexOf(scorePrefix)

        return if (index != -1) {
            response.substring(index + scorePrefix.length).trim().toInt()
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

    protected fun submitAnswer(email: String, questionId: String, answer: String) = runBlocking {
        baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_ANSWER)
            parameter(PARAM_QUESTION_ID, questionId)
            parameter(PARAM_ANSWER, answer)
        }
    }

    protected fun getScore(email: String) = runBlocking {
        baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, ACTION_SCORE)
        }
    }
}