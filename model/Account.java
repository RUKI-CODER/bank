package bank.model;

import java.math.BigDecimal;

public class Account {
    private String accountNo;
    private String customerCode;
    private String accountType;
    private BigDecimal balance;
    private String status;

    public Account() {}

    public Account(String accountNo, String customerCode, String accountType, BigDecimal balance, String status) {
        this.accountNo = accountNo;
        this.customerCode = customerCode;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}