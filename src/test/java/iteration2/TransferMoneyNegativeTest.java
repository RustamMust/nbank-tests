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
import org.junit.jupiter.api.Test;

public class TransferMoneyNegativeTest {

  @BeforeAll
  public static void setupRestAssured() {
    RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
  }

  private String createUserAndGetToken(String username, String password) {
    // создаем пользователя
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
            }
            """
                .formatted(username, password))
        .post("http://localhost:4111/api/v1/admin/users")
        .then()
        .statusCode(HttpStatus.SC_CREATED);

    // логинимся
    return given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(
            """
            {
              "username": "%s",
              "password": "%s"
            }
            """
                .formatted(username, password))
        .post("http://localhost:4111/api/v1/auth/login")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .header("Authorization");
  }

  private int createAccountAndGetId(String token) {
    // создаем аккаунт
    given()
        .header("Authorization", token)
        .contentType(ContentType.JSON)
        .post("http://localhost:4111/api/v1/accounts")
        .then()
        .statusCode(HttpStatus.SC_CREATED);

    // получаем id аккаунта
    return given()
        .header("Authorization", token)
        .contentType(ContentType.JSON)
        .get("http://localhost:4111/api/v1/customer/profile")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .extract()
        .path("accounts[0].id");
  }

  private void depositToAccount(String token, int accountId, int amount) {
    given()
        .header("Authorization", token)
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "id": %d,
              "balance": %d
            }
            """
                .formatted(accountId, amount))
        .post("http://localhost:4111/api/v1/accounts/deposit")
        .then()
        .statusCode(HttpStatus.SC_OK);
  }

  @Test
  public void userCannotTransferNegativeAmount() {
    String password = "Rustam12000!";

    // создаем отправителя
    String senderUsername = "usr" + new Random().nextInt(100000);
    String senderToken = createUserAndGetToken(senderUsername, password);
    int senderAccountId = createAccountAndGetId(senderToken);
    depositToAccount(senderToken, senderAccountId, 1000);

    // создаем получателя
    String receiverUsername = "usr" + new Random().nextInt(100000);
    String receiverToken = createUserAndGetToken(receiverUsername, password);
    int receiverAccountId = createAccountAndGetId(receiverToken);

    // делаем перевод с отрицательной суммой
    given()
        .header("Authorization", senderToken)
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "senderAccountId": %d,
              "receiverAccountId": %d,
              "amount": -1
            }
            """
                .formatted(senderAccountId, receiverAccountId))
        .post("http://localhost:4111/api/v1/accounts/transfer")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(Matchers.equalTo("Transfer amount must be at least 0.01"));
  }

  @Test
  public void userCannotTransferZeroAmount() {
    String password = "Rustam12000!";

    // создаем отправителя
    String senderUsername = "usr" + new Random().nextInt(100000);
    String senderToken = createUserAndGetToken(senderUsername, password);
    int senderAccountId = createAccountAndGetId(senderToken);
    depositToAccount(senderToken, senderAccountId, 1000);

    // создаем получателя
    String receiverUsername = "usr" + new Random().nextInt(100000);
    String receiverToken = createUserAndGetToken(receiverUsername, password);
    int receiverAccountId = createAccountAndGetId(receiverToken);

    // перевод с 0
    given()
        .header("Authorization", senderToken)
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "senderAccountId": %d,
              "receiverAccountId": %d,
              "amount": 0
            }
            """
                .formatted(senderAccountId, receiverAccountId))
        .post("http://localhost:4111/api/v1/accounts/transfer")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(Matchers.equalTo("Transfer amount must be at least 0.01"));
  }

  @Test
  public void userCannotTransferMoreThanLimit() {
    String password = "Rustam12000!";

    // создаем отправителя
    String senderUsername = "usr" + new Random().nextInt(100000);
    String senderToken = createUserAndGetToken(senderUsername, password);
    int senderAccountId = createAccountAndGetId(senderToken);
    depositToAccount(senderToken, senderAccountId, 15000);

    // создаем получателя
    String receiverUsername = "usr" + new Random().nextInt(100000);
    String receiverToken = createUserAndGetToken(receiverUsername, password);
    int receiverAccountId = createAccountAndGetId(receiverToken);

    // перевод больше лимита
    given()
        .header("Authorization", senderToken)
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "senderAccountId": %d,
              "receiverAccountId": %d,
              "amount": 10001
            }
            """
                .formatted(senderAccountId, receiverAccountId))
        .post("http://localhost:4111/api/v1/accounts/transfer")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(Matchers.equalTo("Deposit amount cannot exceed 10000"));
  }
}
