package requests.accounts;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.TransferMoneyRequest;

import static io.restassured.RestAssured.given;

public class TransferMoneyRequester extends Request<TransferMoneyRequest> {
    public TransferMoneyRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public ValidatableResponse post(TransferMoneyRequest requestBody) {
        return given()
                .spec(requestSpecification)
                .body(requestBody)
                .when()
                .post("/api/v1/accounts/transfer")
                .then()
                .spec(responseSpecification);
    }
}
