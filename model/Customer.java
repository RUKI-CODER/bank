package bank.model;

public class Customer {
    private String customerCode;
    private String name;
    private String phone;
    private String address;

    public Customer() {}

    public Customer(String customerCode, String name, String phone, String address) {
        this.customerCode = customerCode;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}