package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited"),
    INVALID_AMOUNT("❌ Please enter a valid amount."),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred"),
    TRANSFER_CANNOT_EXCEED_LIMIT("❌ Error: Transfer amount cannot exceed 10000"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAINS_TWO_WORDS("Name must contain two words with letters only");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
