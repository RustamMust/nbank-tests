package requests.steps;

import io.restassured.specification.RequestSpecification;
import models.GetCustomerProfileResponse;
import models.UpdateCustomerProfileRequest;
import models.UpdateCustomerProfileResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class CustomerSteps {
    public static GetCustomerProfileResponse getCustomerProfile(RequestSpecification requestSpec) {
        return new ValidatedCrudRequester<GetCustomerProfileResponse>(
                requestSpec,
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    public static void updateProfileExpectingError(RequestSpecification requestSpec,
                                                   String invalidName,
                                                   String expectedErrorMessage) {
        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(invalidName)
                .build();

        new CrudRequester(
                requestSpec,
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequestPlainText(expectedErrorMessage)
        ).put(updateRequest);
    }

    public static UpdateCustomerProfileResponse updateProfile(RequestSpecification requestSpec, String newName) {
        UpdateCustomerProfileRequest updateRequest = UpdateCustomerProfileRequest.builder()
                .name(newName)
                .build();

        return new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                requestSpec,
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK()
        ).put(updateRequest);
    }
}
