package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class SessionStorage {
    /* Thread Local - a way to make SessionStorage thread-safe

    Each thread accessing INSTANCE.get() receives its OWN COPY

    Map<Thread, SessionStorage>

    Test1: created users, put them in SessionStorage (OWN COPY1), works with them
    Test2: created users, put them in SessionStorage (OWN COPY2), works with them
    Test3: created users, put them in SessionStorage (OWN COPY3), works with them
    */
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest>users) {
        for (CreateUserRequest user: users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
    }

    /**
     * Returns a CreateUserRequest object by its ordinal number in the list of created users.
     * @param number The ordinal number, starting from 1 (not from 0).
     * @return The CreateUserRequest object corresponding to the specified ordinal number.
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number-1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
    }
}
