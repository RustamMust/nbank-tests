package api.models;

import lombok.Data;

@Data
public class TransactionResponse {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;
}
