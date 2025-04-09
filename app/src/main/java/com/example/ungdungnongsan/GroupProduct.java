package com.example.ungdungnongsan;

import java.util.List;

public class GroupProduct {
	private String groupName;
	private List<Product> products;

	public GroupProduct(String groupName, List<Product> products) {
		this.groupName = groupName;
		this.products = products;
	}

	public String getGroupName() {
		return groupName;
	}

	public List<Product> getProducts() {
		return products;
	}
}
