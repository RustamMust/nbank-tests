package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.admin.AdminCreateUserRequester;
import requests.authentication.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        // 1 - Prepare data for admin user creation
        models.LoginUserRequest userRequest =
                models.LoginUserRequest.builder().username("admin").password("admin").build();

        // 2 - Login user to get "Authorization" token in response header
        new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
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

        // 3 - Login user to get "Authorization" token in response header
        new LoginUserRequester(RequestSpecs.unauthSpec(), ResponseSpecs.requestReturnsOK())
                .post(
                        models.LoginUserRequest.builder()
                                .username(userRequest.getUsername())
                                .password(userRequest.getPassword())
                                .build())
                .header("Authorization", Matchers.notNullValue());
    }
}
