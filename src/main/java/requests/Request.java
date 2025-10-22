package requests;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class Request {
    protected RequestSpecification requestSpecification;
    protected ResponseSpecification responseSpecification;

    public Request(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {

    }
}
