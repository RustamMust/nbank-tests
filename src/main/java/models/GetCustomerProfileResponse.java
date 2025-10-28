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
public class GetCustomerProfileResponse extends BaseModel {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Account {
        private int id;
        private String accountNumber;
        private double balance;
        private List<Transaction> transactions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transaction {
        private int id;
        private double amount;
        private String type;
        private String timestamp;
    }
}
