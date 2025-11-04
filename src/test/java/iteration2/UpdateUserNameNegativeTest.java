package iteration2;

import generators.RandomData;
import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.CustomerSteps;
import specs.RequestSpecs;

public class UpdateUserNameNegativeTest extends BaseTest {

    @Test
    public void userCannotUpdateNameWithSingleWordTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Get name from profile
        String initialName = initialProfile.getName();

        // 4 - Prepare invalid name (single word)
        String invalidName = RandomData.getInvalidNameSingleWord();
        CustomerSteps.updateProfileExpectingError(requestSpec,
                invalidName,
                "Name must contain two words with letters only"
        );

        // 6 - Get customer profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 7 - Assert that name has not changed
        softly.assertThat(updatedProfile.getName())
                .as("Name should not have changed after invalid update")
                .isEqualTo(initialName);
    }
}
