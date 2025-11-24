package iteration1.ui;

import api.models.CreateAccountResponse;
import api.utils.RetryUtils;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = RetryUtils.retry("Create account",
                () -> SessionStorage.getSteps().getAllAccounts(),
                list -> list.size() == 1,
                5,      // max 5 attempts
                2000    // pause 2 seconds
        );

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}