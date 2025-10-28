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

public class TransferMoneyTest extends BaseTest {

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
                                }
                                """
                                .formatted(username, password))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
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
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем id аккаунта
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
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
                .accept(ContentType.JSON)
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

    @ParameterizedTest
    @ValueSource(ints = {1, 9999, 10000})
    public void userCanTransferMoney(int transferAmount) {
        String password = "Rustam12000!";

        // создаем отправителя
        String senderUsername = "usr" + new Random().nextInt(100000);
        String senderToken = createUserAndGetToken(senderUsername, password);
        int senderAccountId = createAccountAndGetId(senderToken);

        // выполняем депозит
        depositToAccount(senderToken, senderAccountId, transferAmount);

        // создаем получателя
        String receiverUsername = "usr" + new Random().nextInt(100000);
        String receiverToken = createUserAndGetToken(receiverUsername, password);
        int receiverAccountId = createAccountAndGetId(receiverToken);

        // получаем балансы до перевода
        float senderBalanceBefore =
                given()
                        .header("Authorization", senderToken)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        float receiverBalanceBefore =
                given()
                        .header("Authorization", receiverToken)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // перевод
        given()
                .header("Authorization", senderToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(
                        """
                                {
                                  "senderAccountId": %d,
                                  "receiverAccountId": %d,
                                  "amount": %d
                                }
                                """
                                .formatted(senderAccountId, receiverAccountId, transferAmount))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", Matchers.equalTo("Transfer successful"))
                .body("senderAccountId", Matchers.equalTo(senderAccountId))
                .body("receiverAccountId", Matchers.equalTo(receiverAccountId))
                .body("amount", Matchers.equalTo((float) transferAmount));

        // получаем балансы после перевода
        float senderBalanceAfter =
                given()
                        .header("Authorization", senderToken)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        float receiverBalanceAfter =
                given()
                        .header("Authorization", receiverToken)
                        .accept(ContentType.JSON)
                        .get("http://localhost:4111/api/v1/customer/profile")
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("accounts[0].balance");

        // проверяем изменения балансов
        Assertions.assertEquals(
                senderBalanceBefore - transferAmount,
                senderBalanceAfter,
                0.001,
                "Sender balance should decrease by transfer amount");

        Assertions.assertEquals(
                receiverBalanceBefore + transferAmount,
                receiverBalanceAfter,
                0.001,
                "Receiver balance should increase by transfer amount");
    }
}
