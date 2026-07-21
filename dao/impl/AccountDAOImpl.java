package bank.dao.impl;

import bank.dao.AccountDAO;
import bank.model.Account;
import bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_no";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account a = new Account();
                a.setAccountNo(rs.getString("account_no"));
                a.setCustomerCode(rs.getString("customer_code"));
                a.setAccountType(rs.getString("account_type"));
                a.setBalance(rs.getBigDecimal("balance"));
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Account findById(String id) {
        String sql = "SELECT * FROM accounts WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountNo(rs.getString("account_no"));
                    a.setCustomerCode(rs.getString("customer_code"));
                    a.setAccountType(rs.getString("account_type"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    a.setStatus(rs.getString("status"));
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insert(Account entity) {
        String sql = "INSERT INTO accounts (account_no, customer_code, account_type, balance, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getAccountNo());
            ps.setString(2, entity.getCustomerCode());
            ps.setString(3, entity.getAccountType());
            ps.setBigDecimal(4, entity.getBalance());
            ps.setString(5, entity.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Account entity) {
        String sql = "UPDATE accounts SET customer_code=?, account_type=?, balance=?, status=? WHERE account_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getCustomerCode());
            ps.setString(2, entity.getAccountType());
            ps.setBigDecimal(3, entity.getBalance());
            ps.setString(4, entity.getStatus());
            ps.setString(5, entity.getAccountNo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM accounts WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Account> search(String keyword) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE account_no LIKE ? OR customer_code LIKE ? OR account_type LIKE ? OR status LIKE ?";
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account a = new Account();
                    a.setAccountNo(rs.getString("account_no"));
                    a.setCustomerCode(rs.getString("customer_code"));
                    a.setAccountType(rs.getString("account_type"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    a.setStatus(rs.getString("status"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ======================================================================
    // ADDED METHOD for auto‑generating next account number
    // ======================================================================
    @Override
    public String findMaxAccountNumber() {
        String sql = "SELECT MAX(account_no) FROM accounts";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}