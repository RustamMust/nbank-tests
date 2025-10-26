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

public class ChangeUserNameTest {
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
  public void userCanChangeName() {
    // создаем юзера
    String username = "usr" + new Random().nextInt(100000);
    String password = "Rustam12000!";
    String userToken = createUserAndGetToken(username, password);

    String newUserName = "Rustam Test";

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

    // убеждаемся, что новое имя отличается от текущего
    Assertions.assertNotEquals(newUserName, initialName, "Initial name should differ from new one");

    // меняем имя у юзера
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
        .statusCode(HttpStatus.SC_OK)
        .body("customer.name", Matchers.equalTo(newUserName))
        .body("message", Matchers.equalTo("Profile updated successfully"));

    // получаем профиль и убеждаемся, что имя действительно обновилось
    given()
        .header("Authorization", userToken)
        .accept(ContentType.JSON)
        .get("http://localhost:4111/api/v1/customer/profile")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("username", Matchers.equalTo(username))
        .body("name", Matchers.equalTo(newUserName));
  }
}
