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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChangeUserNameNegativeTest {
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

  @Test
  public void userCanNotChangeName() {
    // создаем юзера
    String username = "usr" + new Random().nextInt(100000);
    String password = "Rustam12000!";
    String userToken = createUserAndGetToken(username, password);

    String newUserName = "Rustam";

    // делаем GET профиля, чтобы получить исходное имя при создании
    String initialName =
        given()
            .header("Authorization", userToken)
            .get("http://localhost:4111/api/v1/customer/profile")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .jsonPath()
            .getString("name");

    // меняем имя у юзера с невалидными данными
    given()
        .header("Authorization", userToken)
        .contentType(ContentType.JSON)
        .body(
            """
            {
              "name": "%s"
            }
            """
                .formatted(newUserName))
        .put("http://localhost:4111/api/v1/customer/profile")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST)
        .body(Matchers.equalTo("Name must contain two words with letters only"));

    // делаем GET профиля, чтобы получить имя
    String actualName =
        given()
            .header("Authorization", userToken)
            .get("http://localhost:4111/api/v1/customer/profile")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .jsonPath()
            .getString("name");

    // проверяем, что имя осталось прежним
    Assertions.assertEquals(initialName, actualName, "The name should not have changed.");
  }
}
