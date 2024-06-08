import io.ktor.client.statement.*
import io.ktor.http.*
import jdk.jfr.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LoginTest : BaseApiTest() {

    @Test
    @Description("Login attempt with an already registered email.")
    fun testExistingUserLogin() = runBlocking {
        val response = login(staticEmail)
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assert(response.bodyAsText().contains("Error: User is already logged in"))
    }

    @Test
    @Description("Successful registration of a new user.")
    fun testNewUserRegistration() = runBlocking {
        val email = generateRandomEmail()
        val response = login(email)
        assertEquals(HttpStatusCode.OK, response.status)
        assert(response.bodyAsText().contains("You have successfully logged in"))
    }

    @Test
    @Description("Test for invalid email format (missing top-level domain).")
    fun testInvalidEmailFormat() = runBlocking {
        val response = login("testtest.test")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assert(response.bodyAsText().contains("Error: Invalid email address"))
    }


}