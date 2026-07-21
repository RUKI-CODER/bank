package bank.dao.impl;

import bank.dao.TransactionDAO;
import bank.model.Transaction;
import bank.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setTransactionCode(rs.getString("transaction_code"));
                t.setAccountNo(rs.getString("account_no"));
                t.setType(rs.getString("type"));
                t.setAmount(rs.getBigDecimal("amount"));
                t.setDate(rs.getTimestamp("date").toLocalDateTime());
                t.setStatus(rs.getString("status"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Transaction findById(String id) {
        String sql = "SELECT * FROM transactions WHERE transaction_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionCode(rs.getString("transaction_code"));
                    t.setAccountNo(rs.getString("account_no"));
                    t.setType(rs.getString("type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setDate(rs.getTimestamp("date").toLocalDateTime());
                    t.setStatus(rs.getString("status"));
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void insert(Transaction entity) {
        String sql = "INSERT INTO transactions (transaction_code, account_no, type, amount, date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getTransactionCode());
            ps.setString(2, entity.getAccountNo());
            ps.setString(3, entity.getType());
            ps.setBigDecimal(4, entity.getAmount());
            ps.setTimestamp(5, Timestamp.valueOf(entity.getDate()));
            ps.setString(6, entity.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Transaction entity) {
        String sql = "UPDATE transactions SET account_no=?, type=?, amount=?, date=?, status=? WHERE transaction_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getAccountNo());
            ps.setString(2, entity.getType());
            ps.setBigDecimal(3, entity.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            ps.setString(5, entity.getStatus());
            ps.setString(6, entity.getTransactionCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM transactions WHERE transaction_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> search(String keyword) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE transaction_code LIKE ? OR account_no LIKE ? OR type LIKE ? OR status LIKE ?";
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionCode(rs.getString("transaction_code"));
                    t.setAccountNo(rs.getString("account_no"));
                    t.setType(rs.getString("type"));
                    t.setAmount(rs.getBigDecimal("amount"));
                    t.setDate(rs.getTimestamp("date").toLocalDateTime());
                    t.setStatus(rs.getString("status"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}