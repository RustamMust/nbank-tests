package requests.customer;


import static io.restassured.RestAssured.given;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UpdateCustomerProfileRequest;


public class UpdateCustomerProfileRequester extends Request<UpdateCustomerProfileRequest> {
    public UpdateCustomerProfileRequester(
            RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UpdateCustomerProfileRequest model) {
        throw new UnsupportedOperationException("POST not supported for profile");
    }

    public ValidatableResponse put(UpdateCustomerProfileRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
