package com.example.ungdungnongsan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {

	private EditText etName, etPrice, etImageUrl, etDescription, etOrigin, etIngredients, etCategory;
	private Button btnSave;
	private Product product;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit_product);

		etName = findViewById(R.id.etName);
		etPrice = findViewById(R.id.etPrice);
		etImageUrl = findViewById(R.id.etImageUrl);
		etDescription = findViewById(R.id.etDescription);
		etOrigin = findViewById(R.id.etOrigin);
		etIngredients = findViewById(R.id.etIngredients);
		etCategory = findViewById(R.id.etCategory);
		btnSave = findViewById(R.id.btnSave);

		AutoCompleteTextView autoCompleteCategory = (AutoCompleteTextView) etCategory;
		List<String> categoryList = new ArrayList<>();
		categoryList.add("Cá");
		categoryList.add("Rau");
		categoryList.add("Thịt");
		categoryList.add("Trái cây");

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryList);
		autoCompleteCategory.setAdapter(adapter);
		autoCompleteCategory.setThreshold(1);

		// Nếu sửa sản phẩm
		if (getIntent().hasExtra("product")) {
			product = (Product) getIntent().getSerializableExtra("product");
			etName.setText(product.getName());
			etPrice.setText(product.getPrice());
			etImageUrl.setText(product.getImageUrl());
			etDescription.setText(product.getDescription());
			etOrigin.setText(product.getOrigin());
			etIngredients.setText(product.getIngredients());
			autoCompleteCategory.setText(product.getCategory(), false);
		}

		btnSave.setOnClickListener(v -> saveProduct());
	}

	private void saveProduct() {
		String name = etName.getText().toString().trim();
		String price = etPrice.getText().toString().trim();
		String imageUrl = etImageUrl.getText().toString().trim();
		String description = etDescription.getText().toString().trim();
		String origin = etOrigin.getText().toString().trim();
		String ingredients = etIngredients.getText().toString().trim();
		String category = etCategory.getText().toString().trim();

		if (name.isEmpty() || price.isEmpty() || imageUrl.isEmpty() || category.isEmpty()) {
			Toast.makeText(this, "Vui lòng nhập đầy đủ tên, giá, ảnh, danh mục", Toast.LENGTH_SHORT).show();
			return;
		}

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
			return;
		}

		String sellerId = user.getUid();

		DatabaseReference ref = FirebaseDatabase
				                        .getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                        .getReference("products");

		if (product == null) {
			// Thêm mới
			String key = ref.child(category).push().getKey();
			Product newProduct = new Product(name, imageUrl, price, description, origin, ingredients);
			newProduct.setKey(key);
			newProduct.setCategory(category);
			newProduct.setIdSeller(sellerId);

			ref.child(category).child(key).setValue(newProduct);
			Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
		} else {

			if (!product.getCategory().equals(category)) {

				ref.child(product.getCategory()).child(product.getKey()).removeValue();


				String key = ref.child(category).push().getKey();
				Product updatedProduct = new Product(name, imageUrl, price, description, origin, ingredients);
				updatedProduct.setKey(key);
				updatedProduct.setCategory(category);
				updatedProduct.setIdSeller(product.getIdSeller());

				ref.child(category).child(key).setValue(updatedProduct);
			} else {

				product.setName(name);
				product.setPrice(price);
				product.setImageUrl(imageUrl);
				product.setDescription(description);
				product.setOrigin(origin);
				product.setIngredients(ingredients);

				ref.child(category).child(product.getKey()).setValue(product);
			}

			Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
		}

		setResult(RESULT_OK);
		finish();
	}
}
