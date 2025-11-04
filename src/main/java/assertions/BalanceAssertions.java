package assertions;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;

public class BalanceAssertions {
    protected static final Offset<Double> MONEY_TOLERANCE = Offset.offset(0.001);

    public static void assertBalanceUnchanged(SoftAssertions softly, double before, double after) {
        softly.assertThat(after)
                .as("Balance should remain unchanged")
                .isEqualTo(before, MONEY_TOLERANCE);
    }

    public static void assertBalanceIncreasedBy(SoftAssertions softly, double before, double after, double amount) {
        softly.assertThat(after)
                .as("Balance should increase by " + amount)
                .isCloseTo(before + amount, MONEY_TOLERANCE);
    }

    public static void assertBalanceDecreasedBy(SoftAssertions softly, double before, double after, double amount) {
        softly.assertThat(after)
                .as("Balance should decrease by " + amount)
                .isCloseTo(before - amount, MONEY_TOLERANCE);
    }
}
