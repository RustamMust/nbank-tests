package iteration1;

import models.CreateUserRequest;
import models.CreateUserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        // 1 - Prepare data for admin user creation
        models.LoginUserRequest userRequest = models.LoginUserRequest.builder().username("admin").password("admin").build();

        // 2 - Login user to get "Authorization" token in response header
        new ValidatedCrudRequester<CreateUserResponse>
                (RequestSpecs.unauthSpec(),
                        Endpoint.LOGIN,
                        ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 3 - Login user to get "Authorization" token in response header
        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(
                        models.LoginUserRequest.builder()
                                .username(userRequest.getUsername())
                                .password(userRequest.getPassword())
                                .build())
                .header("Authorization", Matchers.notNullValue());
    }
}
