package iteration2;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import generators.RandomData;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateUserNameTest extends BaseTest {

    @Test
    public void userCanUpdateNameTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Get name from profile
        String initialName = initialProfile.getName();

        // 4 - Prepare a new valid name (two words)
        String newUserName = RandomData.getValidName();

        // 5 - Assert new name is different from initial name
        softly.assertThat(newUserName)
                .as("New name should differ from initial name")
                .isNotEqualTo(initialName);

        // 6 - Prepare data for update request
        UpdateCustomerProfileRequest updateRequest =
                UpdateCustomerProfileRequest.builder().name(newUserName).build();

        // 7 - Update profile with valid name
        UpdateCustomerProfileResponse updateResponse = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(requestSpec,
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .put(updateRequest);

        // 8 - Assert update response
        softly.assertThat(updateResponse.getCustomer().getName())
                .as("Response should contain updated name")
                .isEqualTo(newUserName);

        softly.assertThat(updateResponse.getMessage())
                .as("Response should contain success message")
                .isEqualTo("Profile updated successfully");

        // 9 - Get profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 10 - Assert that username has not changed
        softly.assertThat(updatedProfile.getUsername())
                .as("Username should remain unchanged")
                .isEqualTo(userRequest.getUsername());

        // 11 - Assert that name was actually updated in profile
        softly.assertThat(updatedProfile.getName())
                .as("Name should be updated to new value")
                .isEqualTo(newUserName);
    }
}
