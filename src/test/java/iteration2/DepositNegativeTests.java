package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class DepositNegativeTests {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    private String createUserAndGetToken(String username, String password) {
        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                        {
                          "username": "%s",
                          "password": "%s",
                          "role": "USER"
                        }"""
                                .formatted(username, password))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // логин и получение токена
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                        {
                          "username": "%s",
                          "password": "%s"
                        }"""
                                .formatted(username, password))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");
    }

    private int createAccountAndGetId(String token) {
        // создаем аккаунт
        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем id аккаунта
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("accounts[0].id");
    }

    /**
     * Негативные граничные значения для депозита
     * -1, 0
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmounts(int depositAmount) {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);
        int accountId = createAccountAndGetId(token);

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                        {
                          "id": %d,
                          "balance": %d
                        }
                        """
                                .formatted(accountId, depositAmount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Deposit amount must be at least 0.01"));
    }

    /**
     * Негативные граничные значения для депозита
     * 5001
     */
    @ParameterizedTest
    @ValueSource(ints = {5001})
    public void userCannotDepositInvalidLLargeAmounts(int depositAmount) {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);
        int accountId = createAccountAndGetId(token);

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                        {
                          "id": %d,
                          "balance": %d
                        }
                        """
                                .formatted(accountId, depositAmount))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.equalTo("Deposit amount cannot exceed 5000"));
    }

    /**
     * Негативный тест: депозит на чужой аккаунт
     */
    @Test
    public void userCannotDepositToAnotherUserAccount() {
        String user1 = "usr" + new Random().nextInt(100000);
        String user2 = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";

        // создаем двух пользователей
        String token1 = createUserAndGetToken(user1, password);
        String token2 = createUserAndGetToken(user2, password);

        // создаем аккаунт у первого пользователя
        int accountIdUser1 = createAccountAndGetId(token1);

        // второй пользователь пытается внести депозит в аккаунт первого
        given()
                .header("Authorization", token2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                        {
                          "id": %d,
                          "balance": 1000
                        }
                        """
                                .formatted(accountIdUser1))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }

    /**
     * Негативный тест: депозит на несуществующий аккаунт
     */
    @Test
    public void userCannotDepositToNonExistingAccount() {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);

        int nonExistingAccountId = 999999;

        given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                        {
                          "id": %d,
                          "balance": 1000
                        }
                        """
                                .formatted(nonExistingAccountId))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.equalTo("Unauthorized access to account"));
    }
}
