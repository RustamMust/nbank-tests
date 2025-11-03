package requests.steps;

import io.restassured.specification.RequestSpecification;
import models.GetCustomerProfileResponse;
import requests.skeleton.Endpoint;
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
}
