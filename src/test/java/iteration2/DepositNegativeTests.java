package iteration2;

import generators.RandomData;

import io.restassured.specification.RequestSpecification;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.GetCustomerProfileResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.accounts.DepositMoneyRequester;
import requests.customer.GetCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.assertj.core.api.Assertions.offset;

public class DepositNegativeTests extends BaseTest {

    /**
     * Негативный тест: граничные значения для депозита -1, 0
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void userCannotDepositInvalidSmallAmountsTest(int depositAmount) {
        // 1 - Prepare data for user creation
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest createUserRequest =
                CreateUserRequest.builder()
                        .username(username)
                        .password(password)
                        .role(UserRole.USER.toString())
                        .build();

        // 2 - Create a new user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        // 3 - Create an account (null because body not needed)
        RequestSpecification requestSpec = RequestSpecs.authAsUser(username, password);
        new CreateAccountRequester(
                requestSpec,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get customer profile
        GetCustomerProfileResponse customerProfile =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 5 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 6 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 7 - Prepare data for deposit money request
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        // 8 - Try to make invalid deposit
        new DepositMoneyRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsBadRequestPlainText("Deposit amount must be at least 0.01"))
                .post(invalidDeposit);

        // 9 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = new GetCustomerProfileRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 10 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 11 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after invalid deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    /**
     * Негативный тест: граничные значения для депозита 5001
     */
    @ParameterizedTest
    @ValueSource(ints = {5001})
    public void userCannotDepositInvalidLargeAmountsTest(int depositAmount) {
        // 1 - Prepare data for user creation
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest createUserRequest =
                CreateUserRequest.builder()
                        .username(username)
                        .password(password)
                        .role(UserRole.USER.toString())
                        .build();

        // 2 - Create a new user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        // 3 - Create an account (null because body not needed)
        RequestSpecification requestSpec = RequestSpecs.authAsUser(username, password);
        new CreateAccountRequester(
                requestSpec,
                ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get customer profile
        GetCustomerProfileResponse customerProfile =
                new GetCustomerProfileRequester(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        // 5 - Get customer account id from profile
        int accountId = customerProfile.getAccounts().get(0).getId();

        // 6 - Get balance before deposit
        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 7 - Prepare data for deposit money request
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(depositAmount)
                .build();

        // 8 - Try to make invalid deposit
        new DepositMoneyRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsBadRequestPlainText("Deposit amount cannot exceed 5000"))
                .post(invalidDeposit);

        // 9 - Get customer profile after deposit
        GetCustomerProfileResponse updatedProfile = new GetCustomerProfileRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        // 10 - Get balance after deposit
        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 11 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after invalid deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    /**
     * Негативный тест: депозит на чужой аккаунт
     */
    @Test
    public void userCannotDepositToAnotherUserAccountTest() {
        // 1 - Prepare data for two users
        String user1 = RandomData.getUsername();
        String user2 = RandomData.getUsername();
        String password = RandomData.getPassword();

        // 2 - Create both users
        CreateUserRequest createUser1 = CreateUserRequest.builder()
                .username(user1)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        CreateUserRequest createUser2 = CreateUserRequest.builder()
                .username(user2)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUser1);

        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUser2);

        // 3 - Create accounts for both users
        RequestSpecification user1Spec = RequestSpecs.authAsUser(user1, password);
        RequestSpecification user2Spec = RequestSpecs.authAsUser(user2, password);

        new CreateAccountRequester(user1Spec, ResponseSpecs.entityWasCreated())
                .post(null);

        new CreateAccountRequester(user2Spec, ResponseSpecs.entityWasCreated())
                .post(null);

        // 4 - Get user1 profile and balance before deposit
        GetCustomerProfileResponse user1ProfileBefore =
                new GetCustomerProfileRequester(user1Spec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        int user1AccountId = user1ProfileBefore.getAccounts().get(0).getId();
        double initialBalance = user1ProfileBefore.getAccounts().get(0).getBalance();

        // 5 - Prepare deposit request for another user's account
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(user1AccountId)
                .balance(randomBalance)
                .build();

        // 6 - Attempt to deposit into another user's account
        new DepositMoneyRequester(user2Spec,
                ResponseSpecs.requestReturnsForbiddenPlainText("Unauthorized access to account"))
                .post(depositRequest);

        // 7 - Get user1 profile again
        GetCustomerProfileResponse user1ProfileAfter =
                new GetCustomerProfileRequester(user1Spec, ResponseSpecs.requestReturnsOK())
                        .get()
                        .extract()
                        .as(GetCustomerProfileResponse.class);

        double finalBalance = user1ProfileAfter.getAccounts().get(0).getBalance();

        // 8 - Assert that balance remains unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after unauthorized deposit")
                .isEqualTo(initialBalance, offset(0.001));
    }

    /**
     * Негативный тест: депозит на несуществующий аккаунт
     */
    @Test
    public void userCannotDepositToNonExistingAccountTest() {
        // 1 - Prepare data for user creation
        String username = RandomData.getUsername();
        String password = RandomData.getPassword();

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        // 2 - Create user
        new AdminCreateUserRequester(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        // 3 - Create account for created user
        RequestSpecification userSpec = RequestSpecs.authAsUser(username, password);
        new CreateAccountRequester(userSpec, ResponseSpecs.entityWasCreated()).post(null);

        // 4 - Get current user profile and balance
        GetCustomerProfileResponse customerProfile = new GetCustomerProfileRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        double initialBalance = customerProfile.getAccounts().get(0).getBalance();

        // 5 - Prepare invalid deposit for non-existing account
        int nonExistingAccountId = 999999;
        int randomBalance = RandomData.getRandomBalance();
        DepositMoneyRequest invalidDeposit = DepositMoneyRequest.builder()
                .id(nonExistingAccountId)
                .balance(randomBalance)
                .build();

        // 6 - Try deposit
        new DepositMoneyRequester(
                userSpec,
                ResponseSpecs.requestReturnsForbiddenPlainText("Unauthorized access to account"))
                .post(invalidDeposit);

        // 7 - Get profile again
        GetCustomerProfileResponse updatedProfile = new GetCustomerProfileRequester(
                userSpec, ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(GetCustomerProfileResponse.class);

        double finalBalance = updatedProfile.getAccounts().get(0).getBalance();

        // 8 - Assert balance unchanged
        softly.assertThat(finalBalance)
                .as("Balance should not change after trying to deposit to non-existing account")
                .isEqualTo(initialBalance, offset(0.001));
    }
}
