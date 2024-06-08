import com.github.javafaker.Faker
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

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
        return faker.internet().emailAddress()
    }

    protected fun resetUserState(email: String) = runBlocking {
        baseRequest {
            parameter("email", email)
            parameter("action", "reset")
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
            parameter("email", email)
            parameter("action", "login")
        }
    }

    protected fun getNextQuestion(email: String) = runBlocking {
        baseRequest {
            parameter("email", email)
            parameter("action", "question")
        }
    }

    protected fun submitAnswer(email: String, questionId: String, answer: String) = runBlocking {
        baseRequest {
            parameter("email", email)
            parameter("action", "answer")
            parameter("question_id", questionId)
            parameter("answer", answer)
        }
    }

    protected fun getScore(email: String) = runBlocking {
        baseRequest {
            parameter("email", email)
            parameter("action", "score")
        }
    }

    protected fun reset(email: String) = runBlocking {
        baseRequest {
            parameter("email", email)
            parameter("action", "reset")
        }
    }
}