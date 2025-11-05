package requests.steps;

import generators.RandomModelGenerator;
import utils.TestContext;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        // 1 - Prepare data for user creation
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        // 2 - Create a new user
        CreateUserResponse userResponse = new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // 3 - Register created user for later cleanup
        TestContext.registerUser(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userResponse.getId()
        );

        return userRequest;
    }

    public static void deleteUserById(long userId) {
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOK()
        ).delete(userId);
    }
}
