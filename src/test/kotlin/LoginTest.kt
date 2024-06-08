import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginTest : BaseApiTest() {

    @Test
    @Description("Login attempt with an already registered email.")
    fun testExistingUserLogin() = runBlocking {
        val response = login(staticEmail)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: User is already logged in"))
    }

    @Test
    @Description("Successful registration of a new user.")
    fun testNewUserRegistration() = runBlocking {
        val email = generateRandomEmail()
        val response = login(email)
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contentEquals("You have successfully logged in"))
    }

    @Test
    @Description("Test for invalid email format (missing top-level domain).")
    fun testInvalidEmailFormat() = runBlocking {
        val response = login("testtest.test")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid email address"))
    }

    @Test
    @Description("Test for invalid email format (missing dot in domain).")
    fun testInvalidEmailDotFormat() = runBlocking {
        val response = login("test@testtest")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid email address"))
    }

    @Test
    @Description("Missing email parameter.")
    fun testMissingEmailParameter() = runBlocking {
        val response = baseRequest {
            parameter("action", "login")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Email parameter is missing"))
    }

    @Test
    @Description("Missing action parameter")
    fun testMissingActionParameter() = runBlocking {
        val response = baseRequest {
            parameter("email", faker.internet().emailAddress())
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Action parameter is missing"))
    }

    @Test
    @Description("Excessive email length.")
    fun testExcessiveEmailLength() = runBlocking {
        val username = "a".repeat(256)
        val domain = "test.test"
        val email = "$username@$domain"
        val response = login(email)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid email address"))
    }

    @Test
    @Description("Test for invalid email format (incorrect characters).")
    fun testInvalidCharEmailFormat() = runBlocking {
        val response = login("test\"123\"@test.test")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid email address"))
    }

    @Test
    @Description("Empty email.")
    fun testEmptyEmail() = runBlocking {
        val response = login("")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid email address"))
    }

    @Test
    @Description("Invalid action parameter.")
    fun testInvalidActionParameter() = runBlocking {
        val email = generateRandomEmail()
        val response = baseRequest {
            parameter("email", email)
            parameter("action", email)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contentEquals("Error: Invalid action"))
    }
}