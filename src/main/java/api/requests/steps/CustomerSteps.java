package api.requests.steps;

import io.restassured.specification.RequestSpecification;
import api.models.GetCustomerProfileResponse;
import api.models.UpdateCustomerProfileRequest;
import api.models.UpdateCustomerProfileResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

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
