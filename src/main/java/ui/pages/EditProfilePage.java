package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;


@Getter
public class EditProfilePage extends BasePage<EditProfilePage> {
    private final SelenideElement editProfileTitle = $(byText("✏️ Edit Profile"));
    private final SelenideElement newNameInput = $("[placeholder='Enter new name']");
    private final SelenideElement saveChangesButton = $(byText("💾 Save Changes"));
    private final SelenideElement homeButton = $(byText("🏠 Home"));

    @Override
    public String url() {
        return "/profile/edit";
    }

    public EditProfilePage checkEditProfilePageVisible() {
        editProfileTitle.shouldBe(visible);
        return this;
    }

    public EditProfilePage enterNewName(String newName) {
        newNameInput.shouldBe(visible).click();
        newNameInput.clear();
        newNameInput.setValue(newName);
        newNameInput.pressTab();
        return this;
    }

    public EditProfilePage saveChanges() {
        saveChangesButton.shouldBe(visible).click();
        return this;
    }

    public UserDashboard goHome() {
        homeButton.shouldBe(visible).click();
        return new UserDashboard();
    }
}
