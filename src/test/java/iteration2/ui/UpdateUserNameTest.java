package iteration2.ui;

import api.assertions.ProfileAssertions;
import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.GetCustomerProfileResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.CustomerSteps;
import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

public class UpdateUserNameTest extends BaseUiTest {
    @Test
    public void userCanUpdateNameTest() {
        // 1 - Create a new user via API
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update via API
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Get name from profile
        String initialName = initialProfile.getName();

        // 4 - Prepare a new valid name
        String newUserName = RandomData.getValidName();

        // 5 - Assert new name is different from initial name
        ProfileAssertions.assertNewNameIsDifferent(softly, newUserName, initialName);

        // 6 - Set authToken to localStorage via API
        authAsUser(userRequest);

        // 7 - Open dashboard page
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

        // 8 - Get profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 9 - Assert that username has not changed
        ProfileAssertions.assertUsernameUnchanged(softly, userRequest.getUsername(), updatedProfile);

        // 10 - Assert that name changed
        ProfileAssertions.assertNameUpdated(softly, updatedProfile, newUserName);
    }

    @Test
    public void userCannotUpdateNameTest() {
        // 1 - Create a new user via API
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update via API
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Prepare invalid name (single word)
        String invalidName = RandomData.getInvalidNameSingleWord();

        // 4 - Set authToken to localStorage via API
        authAsUser(userRequest);

        // 5 - Open dashboard page
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

        // 6 - Get customer profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 7 - Assert that name has not changed via API
        ProfileAssertions.assertNameUnchanged(softly, initialProfile, updatedProfile);
    }
}
