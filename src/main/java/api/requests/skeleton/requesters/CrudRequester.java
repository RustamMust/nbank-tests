package api.requests.skeleton.requesters;

import api.configs.Config;
import api.models.CreateUserResponse;
import api.requests.skeleton.interfaces.GetAllEndpointInterface;
import api.specs.RequestSpecs;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.HttpRequest;
import api.requests.skeleton.interfaces.CrudEndpointInterface;
import org.apache.http.HttpStatus;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private final static String API_VERSION = Config.getProperty("apiVersion");

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .when()
                .get(API_VERSION + endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("GET запрос на {endpoint} с id {id}")
    public Object get(long id) {
        return null;
    }

    @Override
    @Step("PUT запрос на {endpoint} с телом {model}")
    public ValidatableResponse put(BaseModel model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .when()
                .put(API_VERSION + endpoint.getUrl())
                .then()
                .spec(responseSpecification);
    }

    @Override
    @Step("DELETE запрос на {endpoint} с id {id}")
    public ValidatableResponse delete(long id) {
        return given()
                .spec(requestSpecification)
                .when()
                .delete(API_VERSION + endpoint.getUrl() + "/" + id)
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(API_VERSION + endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpecification);
    }
}
