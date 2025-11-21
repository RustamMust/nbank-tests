package iteration1.api;

import api.generators.RandomData;
import api.helpers.AccountStepsHelper;
import api.models.CreateUserRequest;
import api.models.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountsSteps;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {
    private TransferMoneyResponse transferResponse;

    @Disabled("Need to add DB.")
    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheck() {
        // 1 - Create sender user
        CreateUserRequest senderUser = AdminSteps.createUser();

        // 2 - Create sender account
        var senderSpec = RequestSpecs.authAsUser(senderUser.getUsername(), senderUser.getPassword());
        AccountsSteps.createAccount(senderSpec);

        // 3 - Get sender account id from profile
        int senderAccountId = AccountStepsHelper.getAccountId(senderSpec);

        // 4 - Deposit to sender account
        int depositAmount = RandomData.getRandomBalance();
        AccountsSteps.depositMoney(senderSpec, senderAccountId, depositAmount);

        // 5 - Create receiver user
        CreateUserRequest receiverUser = AdminSteps.createUser();

        // 6 - Create receiver account
        var receiverSpec = RequestSpecs.authAsUser(receiverUser.getUsername(), receiverUser.getPassword());
        AccountsSteps.createAccount(receiverSpec);

        // 7 - Get receiver account id
        int receiverAccountId = AccountStepsHelper.getAccountId(receiverSpec);

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;

        transferResponse = AccountsSteps.transferWithFraudCheck(
                senderSpec,
                senderAccountId,
                receiverAccountId,
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }
}
