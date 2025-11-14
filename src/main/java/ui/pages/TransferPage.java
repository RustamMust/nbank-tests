package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;


@Getter
public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement accountSelect = $x("//option[normalize-space(text())='-- Choose an account --']/ancestor::select");
    private SelenideElement recipientNameInput = $("[placeholder='Enter recipient name']");
    private SelenideElement recipientAccountNumberInput = $("[placeholder='Enter recipient account number']");
    private SelenideElement amountInput = $("[placeholder='Enter amount']");
    private SelenideElement confirmCheckbox = $("#confirmCheck");
    private SelenideElement sendTransferButton = $(byText("🚀 Send Transfer"));
    private SelenideElement makeTransferTitle = $(byText("🔄 Make a Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage chooseAccount(int index) {
        accountSelect.shouldBe(visible).selectOption(index);
        return this;
    }

    public TransferPage enterRecipientName(String name) {
        recipientNameInput.shouldBe(visible).clear();
        recipientNameInput.setValue(name);
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        recipientAccountNumberInput.shouldBe(visible).clear();
        recipientAccountNumberInput.setValue(accountNumber);
        return this;
    }

    public TransferPage enterAmount(int amount) {
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage confirmDetails() {
        confirmCheckbox.shouldBe(visible).click();
        return this;
    }

    public TransferPage sendTransfer() {
        sendTransferButton.click();
        return this;
    }

    public TransferPage checkTransferPageVisible() {
        makeTransferTitle.shouldBe(Condition.visible);
        return this;
    }
}
