package iteration2;

import assertions.BalanceAssertions;
import helpers.AccountStepsHelper;
import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.AccountsSteps;
import specs.RequestSpecs;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 4999, 5000})
    public void userCanDepositMoneyTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get account id from profile
        int accountId = AccountStepsHelper.getAccountId(requestSpec);

        // 4 - Get account balance from profile
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 5 - Assert that initial balance is non-negative
        softly.assertThat(initialBalance)
                .as("Initial balance should be non-negative")
                .isGreaterThanOrEqualTo(0);

        // 6 - Perform deposit
        DepositMoneyResponse depositResponse = AccountsSteps.depositMoney(requestSpec, accountId, depositAmount);

        // 7 - Assert deposit response
        BalanceAssertions.assertBalanceIncreasedBy(softly, initialBalance, depositResponse.getBalance(), depositAmount);
        
        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be null")
                .isNotNull();

        softly.assertThat(depositResponse.getTransactions())
                .as("Transactions list should not be empty")
                .isNotEmpty();

        // 8 - Get transaction from deposit response
        DepositMoneyResponse.Transaction lastTransaction =
                depositResponse.getTransactions().getLast();

        // 9 - Assert transaction info
        softly.assertThat(lastTransaction.getAmount())
                .as("Transaction amount mismatch")
                .isCloseTo(depositAmount, MONEY_TOLERANCE);

        softly.assertThat(lastTransaction.getType())
                .as("Transaction type should be DEPOSIT")
                .isEqualTo(TransactionType.DEPOSIT.name());

        // 10 - Get balance after deposit
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 11 - Assert balance from profile after deposit
        BalanceAssertions.assertBalanceIncreasedBy(softly, initialBalance, finalBalance, depositAmount);
    }
}
