package iteration2.api;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.models.CreateUserRequest;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import iteration1.api.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9999.99, 10000.00})
    public void userCanTransferMoneyTest(double transferAmount) {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id from profile
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit to sender's account (twice, since a single deposit cannot exceed 5000)
        int randomBalance = 5000;
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);
        AccountsSteps.depositMoney(senderSpec, senderAccountId, randomBalance);

        // 5 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account id
        int receiverAccountId = AccountStepsHelper.getAccountId(receiverSpec);

        // 8 - Get sender and receiver balances
        double senderBalanceBefore = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceBefore = AccountStepsHelper.getBalance(receiverSpec);

        // 9 - Perform transfer
        AccountsSteps.transferMoney(senderSpec, senderAccountId, receiverAccountId, transferAmount);

        // 10 - Get sender and receiver balances
        double senderBalanceAfter = AccountStepsHelper.getBalance(senderSpec);
        double receiverBalanceAfter = AccountStepsHelper.getBalance(receiverSpec);

        // 11 - Assert balances
        BalanceAssertions.assertBalanceDecreasedBy(softly, senderBalanceBefore, senderBalanceAfter, transferAmount);
        BalanceAssertions.assertBalanceIncreasedBy(softly, receiverBalanceBefore, receiverBalanceAfter, transferAmount);
    }
}
