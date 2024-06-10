import io.ktor.client.request.*
import io.qameta.allure.Description
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class LoginTest : BaseApiTest() {

    @Test
    @Description("Login attempt with an already registered email.")
    fun testExistingUserLogin() = runBlocking {
        val response = login(staticEmail)
        assertAlreadyLogged(response)
    }

    @Test
    @Description("Successful registration of a new user.")
    fun testNewUserRegistration() = runBlocking {
        val email = generateRandomEmail()
        val response = login(email)
        assertSuccessLogged(response)
    }

    @Test
    @Description("Test for invalid email format (missing top-level domain).")
    fun testInvalidEmailFormat() = runBlocking {
        val domain = generateRandomDomain()
        val response = login(domain)
        assertInvalidEmail(response)
    }

    @Test
    @Description("Test for invalid email format (missing dot in domain).")
    fun testInvalidEmailDotFormat() = runBlocking {
        val email = generateRandomEmail().replace(".", "")
        val response = login(email)
        assertInvalidEmail(response)
    }

    @Test
    @Description("Missing email parameter.")
    fun testMissingEmailParameter() = runBlocking {
        val response = login("")
        assertMissingEmail(response)
    }

    @Test
    @Description("Missing action parameter")
    fun testMissingActionParameter() = runBlocking {
        val email = generateRandomEmail()
        val response = baseRequest {
            parameter(PARAM_EMAIL, email)
        }
        assertMissingAction(response)
    }

    @Test
    @Description("Excessive email length.")
    fun testExcessiveEmailLength() = runBlocking {
        val username = "a".repeat(256)
        val domain = generateRandomDomain()
        val email = "$username@$domain"
        val response = login(email)
        assertInvalidEmail(response)
    }

    @Test
    @Description("Test for invalid email format (incorrect characters).")
    fun testInvalidCharEmailFormat() = runBlocking {
        val domain = generateRandomDomain()
        val email = "test\"123\"@$domain"
        val response = login(email)
        assertInvalidEmail(response)
    }

    @Test
    @Description("Empty email.")
    fun testEmptyEmail() = runBlocking {
        val response = login(" ")
        assertInvalidEmail(response)
    }

    @Test
    @Description("Invalid action parameter.")
    fun testInvalidActionParameter() = runBlocking {
        val email = generateRandomEmail()
        val response = baseRequest {
            parameter(PARAM_EMAIL, email)
            parameter(PARAM_ACTION, email)
        }
        assertInvalidAction(response)
    }
}