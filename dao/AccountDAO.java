package bank.dao;

import bank.model.Account;

public interface AccountDAO extends BaseDAO<Account, String> {

    /**
     * Retrieves the maximum (latest) account number from the database.
     * Used to auto‑generate the next account number.
     * @return the highest account_no as a String, e.g. "ACC125", or null if no accounts exist.
     */
    String findMaxAccountNumber();
}