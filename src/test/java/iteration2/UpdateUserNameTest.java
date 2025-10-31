package iteration2;

import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import generators.RandomData;
import requests.admin.AdminCreateUserRequester;
import requests.customer.GetCustomerProfileRequester;
import requests.customer.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateUserNameTest extends BaseTest {

    @Test
    public void userCanUpdateNameTest() {
        // 1 - Prepare data for user creation
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest createUserRequest =
                CreateUserRequest.builder()
                        .username(username)
                        .password(password)
                        .role(UserRole.USER.toString())
                        .build();

        // 2 - Create a new user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        // 3 - Get customer profile before update
        GetCustomerProfileResponse initialProfile =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 4 - Get name from profile
        String initialName = initialProfile.getName();

        // 5 - Prepare a new valid name (two words)
        String newUserName = RandomData.getValidName();

        // 6 - Assert new name is different from initial name
        softly.assertThat(newUserName)
                .as("New name should differ from initial name")
                .isNotEqualTo(initialName);

        // 7 - Prepare data for update request
        UpdateCustomerProfileRequest updateRequest =
                UpdateCustomerProfileRequest.builder().name(newUserName).build();

        // 8 - Update profile with valid name
        UpdateCustomerProfileResponse updateResponse =
                new UpdateCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .put(updateRequest)
                        .extract()
                        .as(UpdateCustomerProfileResponse.class);

        // 9 - Assert update response
        softly.assertThat(updateResponse.getCustomer().getName())
                .as("Response should contain updated name")
                .isEqualTo(newUserName);

        softly.assertThat(updateResponse.getMessage())
                .as("Response should contain success message")
                .isEqualTo("Profile updated successfully");

        // 10 - Get profile after update
        GetCustomerProfileResponse updatedProfile =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 11 - Assert that username has not changed
        softly.assertThat(updatedProfile.getUsername())
                .as("Username should remain unchanged")
                .isEqualTo(username);

        // 12 - Assert that name was actually updated in profile
        softly.assertThat(updatedProfile.getName())
                .as("Name should be updated to new value")
                .isEqualTo(newUserName);
    }
}
