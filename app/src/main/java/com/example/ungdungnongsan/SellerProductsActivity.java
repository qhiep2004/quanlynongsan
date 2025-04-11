package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ungdungnongsan.Product;
import com.example.ungdungnongsan.SellerProductAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SellerProductsActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private List<Product> productList;
	private SellerProductAdapter adapter;
	private String sellerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seller_products);

		recyclerView = findViewById(R.id.rvSellerProducts);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		productList = new ArrayList<>();
		adapter = new SellerProductAdapter(this, productList);
		recyclerView.setAdapter(adapter);


		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			sellerId = user.getUid();
			loadSellerProducts(sellerId);
		}


		FloatingActionButton btnAddProduct = findViewById(R.id.btnAddProduct);
		btnAddProduct.setOnClickListener(v -> {
			Intent intent = new Intent(SellerProductsActivity.this, AddEditProductActivity.class);
			startActivityForResult(intent, 100); // dùng để reload sau khi thêm
		});
	}


	private void loadSellerProducts(String sellerId) {
		DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                        .getReference("products");

		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				productList.clear();
				for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
					for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
						Product product = productSnapshot.getValue(Product.class);
						if (product != null && sellerId.equals(product.getIdSeller())) {
							productList.add(product);
						}
					}
				}
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(SellerProductsActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100 && resultCode == RESULT_OK) {
			loadSellerProducts(sellerId);
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		loadSellerProducts(sellerId);
	}
}
