package iteration1;

import generators.RandomData;

import java.util.List;
import java.util.stream.Stream;

import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.admin.AdminCreateUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {
        // 1 - Prepare data for user creation
        CreateUserRequest createUserRequest =
                CreateUserRequest.builder()
                        .username(RandomData.getUsername())
                        .password(RandomData.getPassword())
                        .role(UserRole.USER.toString())
                        .build();

        // 2 - Create a new user
        CreateUserResponse createUserResponse =
                new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                        .post(createUserRequest)
                        .extract()
                        .as(CreateUserResponse.class);

        // 3 - Assert body of creation response
        softly.assertThat(createUserRequest.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly
                .assertThat(createUserRequest.getPassword())
                .isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserRequest.getRole()).isEqualTo(createUserResponse.getRole());
    }

    // Prepare invalid user creation data
    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of(
                        "  ",
                        "Password33%",
                        "USER",
                        "username",
                        List.of(
                                "Username must be between 3 and 15 characters",
                                "Username cannot be blank",
                                "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of(
                        "ab",
                        "Password33%",
                        "USER",
                        "username",
                        List.of("Username must be between 3 and 15 characters")),
                Arguments.of(
                        "abc$",
                        "Password33%",
                        "USER",
                        "username",
                        List.of("Username must contain only letters, digits, dashes, underscores, and dots")));
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(
            String username, String password, String role, String errorKey, List<String> errorValue) {
        // 1 - Try to create user with invalid data
        CreateUserRequest createUserRequest =
                CreateUserRequest.builder().username(username).password(password).role(role).build();

        // 2 - Assert error response after request
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}
