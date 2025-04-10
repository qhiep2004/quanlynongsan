package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
	private RecyclerView rvAdminProducts;
	private AdminProductAdapter adapter;
	private List<Product> productList;
	private DatabaseReference productsRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin);

		rvAdminProducts = findViewById(R.id.rvAdminProducts);
		rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
		productList = new ArrayList<>();
		adapter = new AdminProductAdapter(productList, this);
		rvAdminProducts.setAdapter(adapter);

		productsRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				              .getReference("products");

		loadProducts();

		findViewById(R.id.btnAddProduct).setOnClickListener(v -> {
			Intent intent = new Intent(this, AddEditProductActivity.class);
			startActivity(intent);
		});
	}

	private void loadProducts() {
		productList.clear();
		productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
					String categoryName = categorySnapshot.getKey();

					for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
						Product product = productSnapshot.getValue(Product.class);
						if (product != null) {
							product.setKey(productSnapshot.getKey());
							product.setCategory(categoryName);
							productList.add(product);
						}
					}
				}
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {}
		});
	}
}
