package com.example.ungdungnongsan;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

	private TextView tvName, tvPrice, tvDescription, tvOrigin, tvIngredients;
	private ImageView ivProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_detail);

		tvName = findViewById(R.id.tvName);
		tvPrice = findViewById(R.id.tvPrice);
		tvDescription = findViewById(R.id.tvDescription);
		tvOrigin = findViewById(R.id.tvOrigin);
		tvIngredients = findViewById(R.id.tvIngredients);
		ivProduct = findViewById(R.id.ivProduct);

		// Nhận dữ liệu từ Intent
		String name = getIntent().getStringExtra("name");
		String price = getIntent().getStringExtra("price");
		String imageUrl = getIntent().getStringExtra("imageUrl");
		String description = getIntent().getStringExtra("description");
		String origin = getIntent().getStringExtra("origin");
		String ingredients = getIntent().getStringExtra("ingredients");

		tvName.setText(name);
		tvPrice.setText("Giá: " + price);
		tvDescription.setText("Mô tả: " + description);
		tvOrigin.setText("Nguồn gốc: " + origin);
		tvIngredients.setText("Thành phần: " + ingredients);

		Glide.with(this).load(imageUrl).into(ivProduct);
	}
}
