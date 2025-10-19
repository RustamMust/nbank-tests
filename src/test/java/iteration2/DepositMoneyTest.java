package iteration2;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DepositMoneyTest {
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
        .body("balance", Matchers.equalTo((float) depositAmount))
        .body("transactions", Matchers.notNullValue())
        .body("transactions.size()", Matchers.greaterThan(0))
        .body("transactions[0].amount", Matchers.equalTo((float) depositAmount))
        .body("transactions[0].type", Matchers.equalTo("DEPOSIT"));
  }
}
