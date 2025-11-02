package iteration1;

import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.customer.GetCustomerProfileRequester;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 3 - Create an account (null because body not needed)
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        new CrudRequester(
                requestSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get user profile and verify that account exists in the profile
        new GetCustomerProfileRequester(requestSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .body("accounts", Matchers.notNullValue())
                .body("accounts.size()", Matchers.greaterThan(0));
    }
}
