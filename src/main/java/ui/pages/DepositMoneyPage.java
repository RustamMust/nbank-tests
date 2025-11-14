package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@Getter
public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");
    private SelenideElement amountInput = $("[placeholder='Enter amount']");
    private SelenideElement depositButton = $(byText("💵 Deposit"));
    private SelenideElement depositMoneyTitle = $(byText("💰 Deposit Money"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage chooseAccount(int index) {
        accountSelect.shouldBe(visible).selectOption(index);
        return this;
    }

    public DepositMoneyPage enterAmount(int amount) {
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    public DepositMoneyPage submitDeposit() {
        depositButton.click();
        return this;
    }

    public DepositMoneyPage submitInvalidDeposit() {
        depositButton.click();
        return this; // остаемся на этой же странице
    }

    public DepositMoneyPage checkDepositPageVisible() {
        depositMoneyTitle.shouldBe(Condition.visible);
        return this;
    }
}
