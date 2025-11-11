package api.utils;

import java.util.ArrayList;
import java.util.List;

public class TestContext {
    private static final List<TrackedUser> createdUsers = new ArrayList<>();

    public static void registerUser(String username, String password, Long id) {
        createdUsers.add(new TrackedUser(username, password, id));
    }

    public static List<TrackedUser> getCreatedUsers() {
        return createdUsers;
    }

    public static void clear() {
        createdUsers.clear();
    }
}