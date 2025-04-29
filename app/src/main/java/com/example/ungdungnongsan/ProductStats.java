package com.example.ungdungnongsan;

public class ProductStats {
	private String productName;
	private int totalQuantity;
	private int totalRevenue;

	public ProductStats(String productName) {
		this.productName = productName;
		this.totalQuantity = 0;
		this.totalRevenue = 0;
	}

	public void addSales(int quantity, int price) {
		this.totalQuantity += quantity;
		this.totalRevenue += quantity * price;
	}

	public String getProductName() { return productName; }
	public int getTotalQuantity() { return totalQuantity; }
	public int getTotalRevenue() { return totalRevenue; }
}
