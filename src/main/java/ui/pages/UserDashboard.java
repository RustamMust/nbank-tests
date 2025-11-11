package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoneyButton = $(byText("\uD83D\uDCB0 Deposit Money"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public DepositMoneyPage depositMoney() {
        depositMoneyButton.click();
        return getPage(DepositMoneyPage.class);
    }

    public UserDashboard checkUserDashboardVisible() {
        $(byText("User Dashboard")).shouldBe(Condition.visible);
        return this;
    }
}
