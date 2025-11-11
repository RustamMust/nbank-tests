package api.assertions;

import api.models.GetCustomerProfileResponse;
import api.models.UpdateCustomerProfileResponse;
import org.assertj.core.api.SoftAssertions;

public class ProfileAssertions {

    public static void assertNameUpdated(SoftAssertions softly, GetCustomerProfileResponse profile, String expectedName) {
        softly.assertThat(profile.getName())
                .as("Profile name should be updated")
                .isEqualTo(expectedName);
    }

    public static void assertNameUnchanged(SoftAssertions softly, GetCustomerProfileResponse before, GetCustomerProfileResponse after) {
        softly.assertThat(after.getName())
                .as("Name should not have changed")
                .isEqualTo(before.getName());
    }

    public static void assertUsernameUnchanged(SoftAssertions softly, String expectedUsername, GetCustomerProfileResponse profile) {
        softly.assertThat(profile.getUsername())
                .as("Username should remain unchanged")
                .isEqualTo(expectedUsername);
    }

    public static void assertSuccessfulUpdateResponse(SoftAssertions softly, UpdateCustomerProfileResponse response, String expectedName) {
        softly.assertThat(response.getCustomer().getName())
                .as("Response should contain updated name")
                .isEqualTo(expectedName);

        softly.assertThat(response.getMessage())
                .as("Response should contain success message")
                .isEqualTo("Profile updated successfully");
    }

    public static void assertNewNameIsDifferent(SoftAssertions softly, String newName, String oldName) {
        softly.assertThat(newName)
                .as("New name should differ from the old one")
                .isNotEqualTo(oldName);
    }
}
