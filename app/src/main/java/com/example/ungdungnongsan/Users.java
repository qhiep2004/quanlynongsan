package com.example.ungdungnongsan;

public class Users {
	private String email;
	private String password;
	private String name;
	private String phone;
	private String address;
	private String role;
	public Users() {

	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Users(String email, String password, String name, String phone, String address) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.phone = phone;
		this.address = address;
	}


	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getPhone() { return phone; }
	public void setPhone(String phone) { this.phone = phone; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
}
