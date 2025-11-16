package api.dao;

public enum DbTable {
    CUSTOMERS("customers"),
    ACCOUNTS("accounts");

    private final String tableName;

    DbTable(String tableName) {
        this.tableName = tableName;
    }

    public String value() {
        return tableName;
    }
}
