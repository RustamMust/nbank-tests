package iteration2.ui;

import api.assertions.ProfileAssertions;
import api.generators.RandomData;
import api.models.CreateUserRequest;
import api.models.GetCustomerProfileResponse;
import api.models.LoginUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.CustomerSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.*;
import io.restassured.specification.RequestSpecification;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;


import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUserNameTest extends BaseUiTest {
    @Test
    public void userCanUpdateNameTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Get name from profile
        String initialName = initialProfile.getName();

        // 4 - Prepare a new valid name
        String newUserName = RandomData.getValidName();

        // 5 - Assert new name is different from initial name
        ProfileAssertions.assertNewNameIsDifferent(softly, newUserName, initialName);

        // Авторизация через API
        authAsUser(userRequest);

        // 9 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 10 - Click on "Noname" button
        $(Selectors.byText("Noname")).click();

        // 11 - Assert that "Edit Profile" page opened
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        // 12 - Find "Enter new name" input
        SelenideElement enterNewNameInput = $("[placeholder='Enter new name']");
        enterNewNameInput.shouldBe(visible);
        enterNewNameInput.click();
        enterNewNameInput.clear();

        // 13 - Set value to "Enter new name" input
        enterNewNameInput.setValue(newUserName);
        enterNewNameInput.pressTab();

        // 14 - Click on "Save Changes" button
        $(byText("\uD83D\uDCBE Save Changes")).click();

        // 15 - Assert that Successfully updated from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("✅ Name updated successfully!");
        alert.accept();

        // 16 - Assert that after clicking OK, the user still on the "Edit Profile" page
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        // 17 - Click on "Home" button
        $(byText("🏠 Home")).click();

        // 18 - Assert updated name from User Dashboard page
        $(".welcome-text").shouldHave(text("Welcome, " + newUserName + "!"));

        // 19 - Get profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 20 - Assert that username has not changed
        ProfileAssertions.assertUsernameUnchanged(softly, userRequest.getUsername(), updatedProfile);

        // 21 - Assert that name changed
        ProfileAssertions.assertNameUpdated(softly, updatedProfile, newUserName);
    }

    @Test
    public void userCannotUpdateNameTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Get customer profile before update
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        GetCustomerProfileResponse initialProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 3 - Prepare invalid name (single word)
        String invalidName = RandomData.getInvalidNameSingleWord();

        // 4 - Get user auth header
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .extract()
                .header("Authorization");

        // 5 - Open page
        Selenide.open("/");

        // 6 - Set authToken to localStorage
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        // 7 - Open dashboard page
        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // 8 - Click on "Noname" button
        $(Selectors.byText("Noname")).click();

        // 9 - Assert that "Edit Profile" page opened
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        // 10 - Find "Enter new name" input
        SelenideElement enterNewNameInput = $("[placeholder='Enter new name']");
        enterNewNameInput.shouldBe(visible);
        enterNewNameInput.click();
        enterNewNameInput.clear();

        // 11 - Set value to "Enter new name" input
        enterNewNameInput.setValue(invalidName);
        enterNewNameInput.pressTab();

        // 12 - Click on "Save Changes" button
        $(byText("\uD83D\uDCBE Save Changes")).click();

        // 13 - Assert validation message from UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).contains("Name must contain two words with letters only");
        alert.accept();

        // 14 - Assert that after clicking OK, the user still on the "Edit Profile" page
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);

        // 15 - Click on "Home" button
        $(byText("🏠 Home")).click();

        // 16 - Assert username not updated on User Dashboard page
        $(".welcome-text").shouldHave(text("Welcome, noname!"));

        // 17 - Get customer profile after update
        GetCustomerProfileResponse updatedProfile = CustomerSteps.getCustomerProfile(requestSpec);

        // 18 - Assert that name has not changed via API
        ProfileAssertions.assertNameUnchanged(softly, initialProfile, updatedProfile);
    }
}
