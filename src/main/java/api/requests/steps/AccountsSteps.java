package api.requests.steps;

import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;
import io.restassured.specification.RequestSpecification;

public class AccountsSteps {
    public static void createAccount(RequestSpecification userSpec) {
        new CrudRequester(
                userSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null); // null because body not needed
    }

    public static DepositMoneyResponse depositMoney(RequestSpecification userSpec, int accountId, double amount) {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        return new ValidatedCrudRequester<DepositMoneyResponse>(
                userSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK()
        ).post(depositRequest);
    }

    public static void transferMoney(RequestSpecification senderSpec,
                                     int senderAccountId,
                                     int receiverAccountId,
                                     double transferAmount) {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOKWithMessage("Transfer successful")
        ).post(transferRequest);
    }

    public static void transferMoneyExpectingError(RequestSpecification senderSpec,
                                                   int senderAccountId,
                                                   int receiverAccountId,
                                                   double transferAmount,
                                                   String expectedErrorMessage) {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(
                senderSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestPlainText(expectedErrorMessage)
        ).post(transferRequest);
    }

    public static void depositMoneyExpectingError(RequestSpecification userSpec,
                                                  int accountId,
                                                  double amount,
                                                  ErrorType errorType,
                                                  String expectedErrorMessage) {
        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();

        switch (errorType) {
            case BAD_REQUEST -> new CrudRequester(
                    userSpec,
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsBadRequestPlainText(expectedErrorMessage)
            ).post(depositRequest);
            case FORBIDDEN -> new CrudRequester(
                    userSpec,
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsForbiddenPlainText(expectedErrorMessage)
            ).post(depositRequest);
            default -> throw new IllegalArgumentException("Unsupported error type: " + errorType);
        }
    }

    public static TransferMoneyResponse transferWithFraudCheck(RequestSpecification senderSpec, int senderAccountId, int receiverAccountId, double amount) {
        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .description("Test transfer with fraud check")
                    .build();

            return new ValidatedCrudRequester<TransferMoneyResponse>(
                    senderSpec,
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                    ResponseSpecs.requestReturnsOK()).post(transferRequest);

    }
}
