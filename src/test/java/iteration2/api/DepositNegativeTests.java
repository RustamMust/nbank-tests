package iteration2.api;

import api.assertions.BalanceAssertions;
import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import io.restassured.specification.RequestSpecification;
import iteration1.api.BaseTest;
import api.models.CreateUserRequest;
import api.models.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;

public class DepositNegativeTests extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmountsTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer account id from profile
        int accountId = AccountStepsHelper.getAccountId(requestSpec);

        // 4 - Get balance before deposit
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 5 - Try to make invalid deposit
        AccountsSteps.depositMoneyExpectingError(
                requestSpec,
                accountId,
                depositAmount,
                ErrorType.BAD_REQUEST,
                "Deposit amount must be at least 0.01"
        );

        // 6 - Get balance after deposit
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 7 - Assert that balance remains unchanged
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }

    @ParameterizedTest
    @ValueSource(ints = {5001})
    public void userCannotDepositInvalidLargeAmountsTest(int depositAmount) {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer account id from profile
        int accountId = AccountStepsHelper.getAccountId(requestSpec);

        // 4 - Get balance before deposit
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 5 - Try to make invalid deposit
        AccountsSteps.depositMoneyExpectingError(
                requestSpec,
                accountId,
                depositAmount,
                ErrorType.BAD_REQUEST,
                "Deposit amount cannot exceed 5000"
        );

        // 6 - Get balance after deposit
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 7 - Assert that balance remains unchanged
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }

    @Test
    public void userCannotDepositToAnotherUserAccountTest() {
        // 1 - Create two users
        CreateUserRequest user1Request = AdminSteps.createUser();
        CreateUserRequest user2Request = AdminSteps.createUser();

        // 2 - Create accounts for both users
        RequestSpecification user1Spec = RequestSpecs.authAsUser(user1Request.getUsername(), user1Request.getPassword());
        RequestSpecification user2Spec = RequestSpecs.authAsUser(user2Request.getUsername(), user2Request.getPassword());

        AccountsSteps.createAccount(user1Spec);
        AccountsSteps.createAccount(user2Spec);

        // 3 - Get user1 account id and balance before deposit
        int user1AccountId = AccountStepsHelper.getAccountId(user1Spec);
        double initialBalance = AccountStepsHelper.getBalance(user1Spec);

        // 4 - Attempt to deposit into another user's account
        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoneyExpectingError(
                user2Spec,
                user1AccountId,
                randomBalance,
                ErrorType.FORBIDDEN,
                "Unauthorized access to account"
        );

        // 5 - Get balance
        double finalBalance = AccountStepsHelper.getBalance(user1Spec);

        // 6 - Assert that balance remains unchanged
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }

    @Test
    public void userCannotDepositToNonExistingAccountTest() {
        // 1 - Create a new user
        CreateUserRequest userRequest = AdminSteps.createUser();

        // 2 - Create an account
        RequestSpecification requestSpec = RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword());
        AccountsSteps.createAccount(requestSpec);

        // 3 - Get customer balance from profile
        double initialBalance = AccountStepsHelper.getBalance(requestSpec);

        // 4 - Try to deposit
        int nonExistingAccountId = RandomData.getNonExistingAccountId();

        int randomBalance = RandomData.getRandomBalance();
        AccountsSteps.depositMoneyExpectingError(
                requestSpec,
                nonExistingAccountId,
                randomBalance,
                ErrorType.FORBIDDEN,
                "Unauthorized access to account"
        );

        // 5 - Get customer balance from profile
        double finalBalance = AccountStepsHelper.getBalance(requestSpec);

        // 6 - Assert balance unchanged
        BalanceAssertions.assertBalanceUnchanged(softly, initialBalance, finalBalance);
    }
}
