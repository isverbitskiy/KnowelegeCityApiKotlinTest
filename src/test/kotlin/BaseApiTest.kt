import com.github.javafaker.Faker
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach

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

    protected val BASE_URL = "https://qa-test.kcdev.pro"
    protected val staticEmail = "niwatarou@gmail.com"
    protected val client = HttpClient()

    protected suspend fun baseRequest(block: HttpRequestBuilder.() -> Unit): HttpResponse {
        return client.request {
            contentType(ContentType.Application.FormUrlEncoded)
            block()
        }
    }

    @BeforeEach
    fun setup() {
        client.config {
            defaultRequest {
                url(BASE_URL)
            }
        }
    }

    protected fun generateRandomEmail(): String {
        return faker.internet().emailAddress()
    }

    protected fun resetUserState(email: String) = runBlocking {
        client.post("") {
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
        return if (index != 1) {
            response.substring(index + scorePrefix.length).trim().toInt()
        } else {
            throw IllegalStateException("Score not found in response")
        }
    }

    protected fun extactQuestionId(response: String): Int {
        return response.lines().find {
            it.startsWith("Id:")
        }?.replace("Id:", "")?.trim()?.toInt()
            ?: throw IllegalStateException("Id not found in response")
    }
}