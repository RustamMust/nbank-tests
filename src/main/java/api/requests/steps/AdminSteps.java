package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.utils.TestContext;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.apache.http.client.protocol.ResponseAuthCache;

import java.util.List;

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

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class);
    }
}
