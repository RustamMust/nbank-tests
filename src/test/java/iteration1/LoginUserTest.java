package iteration1;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import java.util.List;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoginUserTest {
  @BeforeAll
  public static void setupRestAssured() {
    RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
  }

  @Test
  public void adminCanGenerateAuthTokenTest() {
    given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(
            """
            {
              "username": "admin",
              "password": "admin"
            }""")
        .post("http://localhost:4111/api/v1/auth/login")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .header("Authorization", "Basic YWRtaW46YWRtaW4=");
  }

  @Test
  public void userCanGenerateAuthTokenTest() {
    // создание пользователя
    given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .header("Authorization", "Basic YWRtaW46YWRtaW4=")
        .body(
            """
            {
              "username": "rustam1200",
              "password": "Rustam12000!",
              "role": "USER"
            }""")
        .post("http://localhost:4111/api/v1/admin/users")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_CREATED);

    given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body(
            """
            {
              "username": "rustam12000",
              "password": "Rustam12000!"
            }""")
        .post("http://localhost:4111/api/v1/auth/login")
        .then()
        .assertThat()
        .statusCode(HttpStatus.SC_OK)
        .header("Authorization", Matchers.notNullValue());
  }
}
