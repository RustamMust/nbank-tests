package iteration1;

import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.GetCustomerProfileResponse;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account (null because body not needed)
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        new CrudRequester(
                requestSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 3 - Get user profile
        GetCustomerProfileResponse customerProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(
                requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 4 - Assert that profile has accounts and at least one exists
        softly.assertThat(customerProfile.getAccounts())
                .as("User should have at least one account in profile")
                .isNotNull()
                .isNotEmpty();
    }
}
