package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;

public class DepositMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    @Test
    public void userCanDepositMoneyTest() {
        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(
                        """
                                {
                                  "username": "rustam1223",
                                  "password": "Rustam12000!",
                                  "role": "USER"
                                }"""
                )
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "username": "rustam1223",
                                  "password": "Rustam12000!"
                                }"""
                )
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

        // запрашиваем созданный профиль юзера и проверяем, что в нем есть созданный аккаунт
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("accounts", Matchers.notNullValue());

        // получаем id аккаунта из профиля
        int accountId =
                given()
                        .header("Authorization", userAuthHeader)
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .when()
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].id");

        // делаем депозит денег на аккаунт
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "id": %d,
                                  "balance": 1000
                                }
                                """.formatted(accountId)
                )
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                // Проверяем, что баланс равен 1000.0
                .body("balance", Matchers.equalTo(1000.0F))
                // Проверяем, что массив transactions не null
                .body("transactions", Matchers.notNullValue())
                // Проверяем, что в массиве транзакций есть хотя бы 1 элемент
                .body("transactions.size()", Matchers.greaterThan(0))
                // Проверяем, что у первой транзакции amount = 1000.0 и type = "DEPOSIT"
                .body("transactions[0].amount", Matchers.equalTo(1000.0F))
                .body("transactions[0].type", Matchers.equalTo("DEPOSIT"));

    }


}




