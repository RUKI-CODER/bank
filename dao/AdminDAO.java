package bank.dao;

import bank.model.Admin;

public interface AdminDAO extends BaseDAO<Admin, Integer> {
    // Additional method for login
    Admin findByUsernameAndPassword(String username, String password);
}