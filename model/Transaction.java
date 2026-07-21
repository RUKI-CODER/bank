package bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private String transactionCode;
    private String accountNo;
    private String type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String status;

    public Transaction() {}

    public Transaction(String transactionCode, String accountNo, String type, BigDecimal amount, LocalDateTime date, String status) {
        this.transactionCode = transactionCode;
        this.accountNo = accountNo;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}