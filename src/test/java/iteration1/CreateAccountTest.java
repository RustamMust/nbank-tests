package iteration1;

import static io.restassured.RestAssured.given;

import generators.RandomData;
import io.restassured.http.ContentType;
import models.CreateUserRequest;
import models.UserRole;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.admin.AdminCreateUserRequester;
import requests.accounts.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // 1 - Prepare data for user creation
        CreateUserRequest userRequest =
                CreateUserRequest.builder()
                        .username(RandomData.getUsername())
                        .password(RandomData.getPassword())
                        .role(UserRole.USER.toString())
                        .build();

        // 2 - Create a new user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // 3 - Create an account (null because creating account do not need body)
        new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null);

        // запросить аккаунт пользователя и проверить, что созданный аккаунт там
//        given()
//                .header("Authorization", userAuthHeader)
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .get("http://localhost:4111/api/v1/customer/profile")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .body("accounts", Matchers.notNullValue());
    }
}
