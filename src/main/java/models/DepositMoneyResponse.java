package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transaction {
        private int id;
        private double amount;
        private String type;
        private String timestamp;
        private int relatedAccountId;
    }
}
