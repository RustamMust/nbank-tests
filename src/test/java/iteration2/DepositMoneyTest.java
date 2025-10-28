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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DepositMoneyTest extends BaseTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        // генерируем уникальный username один раз
        String username = "usr" + new Random().nextInt(100000);
        String password = "Rustam12000!";

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

        // получаем токен юзера
        String userAuthHeader =
                given()
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

        // создаем аккаунт (счет)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // запрашиваем созданный профиль
        var profileBefore =
                given()
                        .header("Authorization", userAuthHeader)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract();

        // получаем id аккаунта
        int accountId = profileBefore.path("accounts[0].id");

        // получаем баланс до депозита
        float initialBalance = profileBefore.path("accounts[0].balance");

        // убеждаемся, что баланс до депозита 0 (или неотрицательный)
        Assertions.assertTrue(initialBalance >= 0, "Initial balance should be non-negative");

        // делаем депозит
        given()
                .header("Authorization", userAuthHeader)
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
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(initialBalance + depositAmount))
                .body("transactions", Matchers.notNullValue())
                .body("transactions.size()", Matchers.greaterThan(0))
                .body("transactions[0].amount", Matchers.equalTo((float) depositAmount))
                .body("transactions[0].type", Matchers.equalTo("DEPOSIT"));

        // получаем профиль после депозита
        var profileAfter =
                given()
                        .header("Authorization", userAuthHeader)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract();

        // получаем баланс после депозита
        float finalBalance = profileAfter.path("accounts[0].balance");

        // проверяем, что баланс увеличился на сумму депозита
        Assertions.assertEquals(
                initialBalance + depositAmount,
                finalBalance,
                0.001,
                "Balance after deposit should increase by deposit amount");
    }
}
