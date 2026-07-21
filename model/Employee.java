package bank.model;

public class Employee {
    private String employeeCode;
    private String name;
    private String username;
    private String phone;
    private String password;
    private String status;

    // Constructors, getters, and setters

    public Employee() {}

    public Employee(String employeeCode, String name, String username, String phone, String password, String status) {
        this.employeeCode = employeeCode;
        this.name = name;
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.status = status;
    }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}