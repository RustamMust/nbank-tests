package iteration1.api;

import org.assertj.core.data.Offset;
import api.utils.TestContext;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import api.requests.steps.AdminSteps;

public class BaseTest {
    protected static final Offset<Double> MONEY_TOLERANCE = Offset.offset(0.001);
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
        TestContext.clear();
    }

    @AfterEach
    public void afterTest() {
        try {
            cleanupCreatedUsers();
        } finally {
            softly.assertAll();
        }
    }

    private void cleanupCreatedUsers() {
        for (var user : TestContext.getCreatedUsers()) {
            try {
                AdminSteps.deleteUserById(user.getId());
                System.out.println("Deleted user with id " + user.getId() + " (" + user.getUsername() + ")");
            } catch (Exception e) {
                System.out.println("Failed to delete user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
}