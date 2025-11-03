package iteration2;

import generators.RandomData;
import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateUserNameNegativeTest extends BaseTest {

    @Test
    public void userCannotUpdateNameWithSingleWordTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 3 - Get name from profile
        String initialName = initialProfile.getName();

        // 4 - Prepare invalid name (single word)
        String invalidName = RandomData.getInvalidNameSingleWord();
        UpdateCustomerProfileRequest updateRequest =
                UpdateCustomerProfileRequest.builder().name(invalidName).build();

        // 5 - Try to update profile with invalid name
        new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                requestSpec,
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequestPlainText("Name must contain two words with letters only")
        ).put(updateRequest);

        // 6 - Get customer profile after update
        GetCustomerProfileResponse updatedProfile = new ValidatedCrudRequester<GetCustomerProfileResponse>(requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();

        // 7 - Assert that name has not changed
        softly.assertThat(updatedProfile.getName())
                .as("Name should not have changed after invalid update")
                .isEqualTo(initialName);
    }
}
