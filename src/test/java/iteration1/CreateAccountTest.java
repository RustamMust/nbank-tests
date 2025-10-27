package iteration1;

import generators.RandomData;
import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.accounts.CreateAccountRequester;
import requests.admin.AdminCreateUserRequester;
import requests.customer.GetCustomerProfileRequester;
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

        // 3 - Create an account (null because body not needed)
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        new CreateAccountRequester(
                requestSpec,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get user profile and verify that account exists in the profile
        new GetCustomerProfileRequester(requestSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .body("accounts", Matchers.notNullValue())
                .body("accounts.size()", Matchers.greaterThan(0));
    }
}
