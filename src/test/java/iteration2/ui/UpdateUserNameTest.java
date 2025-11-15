package iteration2.ui;

import api.assertions.ProfileAssertions;
import api.generators.RandomData;
import api.models.GetCustomerProfileResponse;
import api.requests.steps.CustomerSteps;
import api.specs.RequestSpecs;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class UpdateUserNameTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanUpdateNameTest() {
        var user = SessionStorage.getUser();

        var requestSpec = RequestSpecs.authAsUser(user.getUsername(), user.getPassword());

        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);
        String initialName = initialProfile.getName();

        String newUserName = RandomData.getValidName();

        ProfileAssertions.assertNewNameIsDifferent(softly, newUserName, initialName);

        new UserDashboard()
                .open()
                .openProfile()
                .checkEditProfilePageVisible()
                .enterNewName(newUserName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .checkEditProfilePageVisible()
                .goHome()
                .checkWelcomeText(newUserName);

        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        ProfileAssertions.assertUsernameUnchanged(softly, user.getUsername(), updatedProfile);
        ProfileAssertions.assertNameUpdated(softly, updatedProfile, newUserName);
    }

    @Test
    @UserSession
    public void userCannotUpdateNameTest() {
        var user = SessionStorage.getUser();

        var requestSpec = RequestSpecs.authAsUser(user.getUsername(), user.getPassword());

        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        String invalidName = RandomData.getInvalidNameSingleWord();

        new UserDashboard()
                .open()
                .openProfile()
                .checkEditProfilePageVisible()
                .enterNewName(invalidName)
                .saveChanges()
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAINS_TWO_WORDS.getMessage())
                .checkEditProfilePageVisible()
                .goHome()
                .checkWelcomeText("noname");

        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        ProfileAssertions.assertNameUnchanged(softly, initialProfile, updatedProfile);
    }
}
