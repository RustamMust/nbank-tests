package iteration1.api;

import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.LoginUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        // 1 - Prepare data for admin user creation
        LoginUserRequest userRequest = LoginUserRequest.builder().username("admin").password("admin").build();

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
                        LoginUserRequest.builder()
                                .username(userRequest.getUsername())
                                .password(userRequest.getPassword())
                                .build())
                .header("Authorization", Matchers.notNullValue());
    }
}
