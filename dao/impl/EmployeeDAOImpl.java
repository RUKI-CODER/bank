package bank.dao.impl;

import bank.dao.EmployeeDAO;
import bank.model.Employee;
import bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl implements EmployeeDAO {

    @Override
    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY employee_code";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Employee e = new Employee();
                e.setEmployeeCode(rs.getString("employee_code"));
                e.setName(rs.getString("name"));
                e.setUsername(rs.getString("username"));
                e.setPhone(rs.getString("phone"));
                e.setPassword(rs.getString("password"));
                e.setStatus(rs.getString("status"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public Employee findById(String id) {
        String sql = "SELECT * FROM employees WHERE employee_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee e = new Employee();
                    e.setEmployeeCode(rs.getString("employee_code"));
                    e.setName(rs.getString("name"));
                    e.setUsername(rs.getString("username"));
                    e.setPhone(rs.getString("phone"));
                    e.setPassword(rs.getString("password"));
                    e.setStatus(rs.getString("status"));
                    return e;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void insert(Employee entity) {
        String sql = "INSERT INTO employees (employee_code, name, username, phone, password, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getEmployeeCode());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getUsername());
            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getPassword());
            ps.setString(6, entity.getStatus());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void update(Employee entity) {
        String sql = "UPDATE employees SET name=?, username=?, phone=?, password=?, status=? WHERE employee_code=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getUsername());
            ps.setString(3, entity.getPhone());
            ps.setString(4, entity.getPassword());
            ps.setString(5, entity.getStatus());
            ps.setString(6, entity.getEmployeeCode());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM employees WHERE employee_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<Employee> search(String keyword) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE name LIKE ? OR employee_code LIKE ? OR username LIKE ? OR phone LIKE ? OR status LIKE ?";
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee e = new Employee();
                    e.setEmployeeCode(rs.getString("employee_code"));
                    e.setName(rs.getString("name"));
                    e.setUsername(rs.getString("username"));
                    e.setPhone(rs.getString("phone"));
                    e.setPassword(rs.getString("password"));
                    e.setStatus(rs.getString("status"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
}