package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.GetCustomerProfileResponse;
import models.UpdateCustomerProfileRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.admin.AdminCreateUserRequester;
import requests.customer.GetCustomerProfileRequester;
import requests.customer.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeUserNameNegativeTest extends BaseTest {

    @Test
    public void userCannotUpdateNameWithSingleWordTest() {
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

        // 5 - Prepare invalid name (single word)
        String invalidName = RandomData.getInvalidNameSingleWord();
        UpdateCustomerProfileRequest updateRequest =
                UpdateCustomerProfileRequest.builder().name(invalidName).build();

        // 6 - Try to update profile with invalid name
        new UpdateCustomerProfileRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsBadRequestPlainText("Name must contain two words with letters only")
        ).put(updateRequest);

        // 7 - Get customer profile after update
        GetCustomerProfileResponse updatedProfile =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 8 - Verify name has not changed
        softly.assertThat(updatedProfile.getName())
                .as("Name should not have changed after invalid update")
                .isEqualTo(initialName);
    }
}
