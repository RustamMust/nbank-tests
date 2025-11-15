package api.utils;

import java.util.ArrayList;
import java.util.List;

public class TestContext {
    /* Thread Local - a way to make TestContext thread-safe

    Each thread accessing INSTANCE.get() receives its OWN COPY

    Map<Thread, TestContext>

    Test1: created users, put them in TestContext (OWN COPY1), works with them
    Test2: created users, put them in TestContext (OWN COPY2), works with them
    Test3: created users, put them in TestContext (OWN COPY3), works with them
    */
    private static final ThreadLocal<TestContext> INSTANCE = ThreadLocal.withInitial(TestContext::new);

    private final List<TrackedUser> createdUsers = new ArrayList<>();

    private TestContext() {}

    public static void registerUser(String username, String password, Long id) {
        INSTANCE.get().createdUsers.add(new TrackedUser(username, password, id));
    }

    public static List<TrackedUser> getCreatedUsers() {
        return INSTANCE.get().createdUsers;
    }

    public static void clear() {
        INSTANCE.get().createdUsers.clear();
    }
}