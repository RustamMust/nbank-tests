package iteration2;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;

import java.util.List;
import java.util.Random;

import iteration1.BaseTest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DepositNegativeTests extends BaseTest {
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
     * Негативные граничные значения для депозита -1, 0
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmounts(int depositAmount) {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);
        int accountId = createAccountAndGetId(token);

        // получаем баланс до депозита
        float initialBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // попытка депозита
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

        // получаем баланс после депозита
        float finalBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // проверяем, что баланс не изменился
        Assertions.assertEquals(
                initialBalance, finalBalance, 0.001, "Balance should not change after invalid deposit");
    }

    /**
     * Негативные граничные значения для депозита 5001
     */
    @ParameterizedTest
    @ValueSource(ints = {5001})
    public void userCannotDepositInvalidLargeAmounts(int depositAmount) {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);
        int accountId = createAccountAndGetId(token);

        // получаем баланс до депозита
        float initialBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // попытка депозита
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

        // получаем баланс после депозита
        float finalBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // проверяем, что баланс не изменился
        Assertions.assertEquals(
                initialBalance, finalBalance, 0.001, "Balance should not change after invalid deposit");
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

        // получаем баланс до попытки депозита
        float initialBalance =
                given()
                        .header("Authorization", token1)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

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

        // получаем баланс после депозита
        float finalBalance =
                given()
                        .header("Authorization", token1)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // проверяем, что баланс не изменился
        Assertions.assertEquals(
                initialBalance,
                finalBalance,
                0.001,
                "Balance should not change after unauthorized deposit");
    }

    /**
     * Негативный тест: депозит на несуществующий аккаунт
     */
    @Test
    public void userCannotDepositToNonExistingAccount() {
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";
        String token = createUserAndGetToken(username, password);
        // создаем реальный аккаунт, чтобы можно было считать баланс
        createAccountAndGetId(token);

        int nonExistingAccountId = 999999;

        // получаем баланс до депозита
        float initialBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // выполняем попытку депозита в несуществующий аккаунт
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

        // получаем баланс после
        float finalBalance =
                given()
                        .header("Authorization", token)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // проверяем, что баланс не изменился
        Assertions.assertEquals(
                initialBalance, finalBalance, 0.001, "Balance should not change after invalid deposit");
    }
}
