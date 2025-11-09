package iteration1.api;

import io.restassured.specification.RequestSpecification;
import models.CreateUserRequest;
import models.GetCustomerProfileResponse;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import requests.steps.AccountsSteps;
import specs.RequestSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get user profile
        GetCustomerProfileResponse customerProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 4 - Assert that profile has accounts and at least one exists
        softly.assertThat(customerProfile.getAccounts())
                .as("User should have at least one account in profile")
                .isNotNull()
                .isNotEmpty();
    }
}
