package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import java.util.List;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_CREATED).build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder().expectStatusCode(HttpStatus.SC_OK).build();
    }

    public static ResponseSpecification requestReturnsBadRequest(
            String errorKey, List<String> errorValues) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.equalTo(errorValues))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequestPlainText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbiddenPlainText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsOKWithMessage(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("message", Matchers.equalTo(expectedMessage))
                .build();
    }
}
