package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest>users) {
        for (CreateUserRequest user: users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Returns a CreateUserRequest object by its ordinal number in the list of created users.
     * @param number The ordinal number, starting from 1 (not from 0).
     * @return The CreateUserRequest object corresponding to the specified ordinal number.
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number-1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
    }
}
