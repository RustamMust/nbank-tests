package iteration2;

import assertions.ProfileAssertions;
import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import generators.RandomData;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import specs.RequestSpecs;

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

        // 4 - Prepare a new valid name
        String newUserName = RandomData.getValidName();

        // 5 - Assert new name is different from initial name
        ProfileAssertions.assertNewNameIsDifferent(softly, newUserName, initialName);

        // 6 - Update profile with valid name
        UpdateCustomerProfileResponse updateResponse =
                CustomerSteps.updateProfile(requestSpec, newUserName);

        // 7 - Assert update response
        ProfileAssertions.assertSuccessfulUpdateResponse(softly, updateResponse, newUserName);

        // 8 - Get profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 9 - Assert that username has not changed
        ProfileAssertions.assertUsernameUnchanged(softly, userRequest.getUsername(), updatedProfile);

        // 10 - Assert that name changed
        ProfileAssertions.assertNameUpdated(softly, updatedProfile, newUserName);
    }
}
