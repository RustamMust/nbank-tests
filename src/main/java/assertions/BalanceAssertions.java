package assertions;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Offset;

public class BalanceAssertions {
    public static void assertBalanceUnchanged(SoftAssertions softly, double before, double after) {
        softly.assertThat(after)
                .as("Balance should remain unchanged")
                .isEqualTo(before, Offset.offset(0.001));
    }
}
