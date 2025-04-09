package com.example.ungdungnongsan;

public class Product {
	private String name;
	private String imageUrl;
	private String price;
	private String description;
	private String origin;
	private String ingredients;
	public Product() {}

	public Product(String name, String imageUrl, String price, String description, String origin, String ingredients) {
		this.name = name;
		this.imageUrl = imageUrl;
		this.price = price;
		this.description = description;
		this.origin = origin;
		this.ingredients = ingredients;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getName() { return name; }
	public String getImageUrl() { return imageUrl; }
	public String getPrice() { return price; }
}
