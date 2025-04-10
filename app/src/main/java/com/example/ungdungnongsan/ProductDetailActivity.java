package com.example.ungdungnongsan;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

	private TextView tvName, tvPrice, tvDescription, tvOrigin, tvIngredients;
	private ImageView ivProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_detail);


		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("");

		tvName = findViewById(R.id.tvName);
		tvPrice = findViewById(R.id.tvPrice);
		tvDescription = findViewById(R.id.tvDescription);
		tvOrigin = findViewById(R.id.tvOrigin);
		tvIngredients = findViewById(R.id.tvIngredients);
		ivProduct = findViewById(R.id.ivProduct);


		Product product = (Product) getIntent().getSerializableExtra("product");

		if (product != null) {
			tvName.setText(product.getName());
			tvPrice.setText("Giá: " + product.getPrice());
			tvDescription.setText("Mô tả: " + product.getDescription());
			tvOrigin.setText("Nguồn gốc: " + product.getOrigin());
			tvIngredients.setText("Thành phần: " + product.getIngredients());

			Glide.with(this).load(product.getImageUrl()).into(ivProduct);
		}
	}


	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}
}
