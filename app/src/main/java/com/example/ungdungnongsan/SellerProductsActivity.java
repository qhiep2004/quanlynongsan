package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Button;
public class SellerProductsActivity extends AppCompatActivity {

	private RecyclerView recyclerView;
	private List<Product> productList;
	private List<Product> filteredList;
	private SellerProductAdapter adapter;
	private String sellerId;
	private EditText etSearch;
	private LinearLayout categoryLayout;
	private String currentCategory = "Tất cả";
	private Map<String, List<Product>> categorizedProducts;
	private TextView tvCategoryTitle;
	private TextView tvSeeAll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seller_products);

		recyclerView = findViewById(R.id.rvSellerProducts);

		GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
		recyclerView.setLayoutManager(layoutManager);

		productList = new ArrayList<>();
		filteredList = new ArrayList<>();
		categorizedProducts = new HashMap<>();


		adapter = new SellerProductAdapter(this, filteredList);
		adapter.setSellerView(true);
		recyclerView.setAdapter(adapter);

		etSearch = findViewById(R.id.etSearch);
		categoryLayout = findViewById(R.id.categoryLayout);
		tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
		tvSeeAll = findViewById(R.id.tvSeeAll);

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			sellerId = user.getUid();
			loadSellerProducts(sellerId);
		}


		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				filterProducts(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

		tvSeeAll.setOnClickListener(v -> {
			currentCategory = "Tất cả";
			tvCategoryTitle.setText("Tất cả");
			filterProducts(etSearch.getText().toString());


			for (int i = 0; i < categoryLayout.getChildCount(); i++) {
				Button button = (Button) categoryLayout.getChildAt(i);
				button.setSelected(button.getText().equals("Tất cả"));
			}

			Toast.makeText(this, "Xem tất cả sản phẩm", Toast.LENGTH_SHORT).show();
		});

		String[] categories = {"Tất cả", "Cá", "Rau", "Thịt", "Trái cây"};
		for (String category : categories) {
			Button btn = new Button(this);
			btn.setText(category);
			btn.setAllCaps(false);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
			);
			params.setMargins(8, 0, 8, 0);
			btn.setLayoutParams(params);

			btn.setBackgroundResource(R.drawable.category_button_background);

			btn.setContentDescription("Danh mục " + category);
			btn.setOnClickListener(v -> {
				currentCategory = category;
				tvCategoryTitle.setText(category);
				filterProducts(etSearch.getText().toString());

				for (int i = 0; i < categoryLayout.getChildCount(); i++) {
					Button button = (Button) categoryLayout.getChildAt(i);
					button.setSelected(button.getText().equals(category));
				}
			});

			if ("Tất cả".equals(category)) {
				btn.setSelected(true);
			}

			categoryLayout.addView(btn);
		}

		FloatingActionButton btnAddProduct = findViewById(R.id.btnAddProduct);
		btnAddProduct.setOnClickListener(v -> {
			Intent intent = new Intent(SellerProductsActivity.this, AddEditProductActivity.class);
			startActivityForResult(intent, 100);
		});

		Button btnLogout = findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();


			Intent intent = new Intent(SellerProductsActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		});

		Button btnSellerInfo = findViewById(R.id.btnSellerInfo);
		btnSellerInfo.setOnClickListener(v -> {
			showSellerInfo();
		});


	}

	private void showSellerInfo() {
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
			return;
		}

		String uid = currentUser.getUid();
		DatabaseReference userRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                            .getReference("users").child(uid);

		userRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()) {
					String name = snapshot.child("name").getValue(String.class);
					String email = snapshot.child("email").getValue(String.class);
					String phone = snapshot.child("phone").getValue(String.class);
					String address = snapshot.child("address").getValue(String.class);

					View dialogView = getLayoutInflater().inflate(R.layout.layout_seller_info, null);
					TextView tvName = dialogView.findViewById(R.id.tvName);
					TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
					TextView tvPhone = dialogView.findViewById(R.id.tvPhone);
					TextView tvAddress = dialogView.findViewById(R.id.tvAddress);
					Button btnEditInfo = dialogView.findViewById(R.id.btnEditInfo);

					tvName.setText("Tên: " + (name != null ? name : ""));
					tvEmail.setText("Email: " + (email != null ? email : ""));
					tvPhone.setText("SĐT: " + (phone != null ? phone : ""));
					tvAddress.setText("Địa chỉ: " + (address != null ? address : ""));

					androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(SellerProductsActivity.this)
							                                            .setView(dialogView)
							                                            .setPositiveButton("Đóng", null)
							                                            .create();

					dialog.show();

					btnEditInfo.setOnClickListener(v -> {
						dialog.dismiss();

						Intent intent = new Intent(SellerProductsActivity.this, EditProfileActivity1.class);
						startActivity(intent);
					});
				} else {
					Toast.makeText(SellerProductsActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(SellerProductsActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void loadSellerProducts(String sellerId) {
		DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                        .getReference("products");

		ref.addValueEventListener(new ValueEventListener() { // Thay đổi từ addListenerForSingleValueEvent sang addValueEventListener để lắng nghe thay đổi liên tục
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				productList.clear();
				categorizedProducts.clear();

				String[] categories = {"Cá", "Rau", "Thịt", "Trái cây"};
				for (String category : categories) {
					categorizedProducts.put(category, new ArrayList<>());
				}

				for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
					for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
						Product product = productSnapshot.getValue(Product.class);
						String productId = productSnapshot.getKey(); // Lấy ID của sản phẩm từ key
						if (product != null && sellerId.equals(product.getIdSeller())) {
							// Gán ID cho đối tượng Product
							product.setId(productId);
							productList.add(product);

							String category = product.getCategory();
							List<Product> categoryList = categorizedProducts.get(category);
							if (categoryList != null) {
								categoryList.add(product);
							}
						}
					}
				}

				filterProducts(etSearch.getText().toString());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Toast.makeText(SellerProductsActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void filterProducts(String query) {
		filteredList.clear();

		List<Product> productsToFilter;
		if ("Tất cả".equals(currentCategory)) {
			productsToFilter = productList;
		} else {
			productsToFilter = categorizedProducts.get(currentCategory);
			if (productsToFilter == null) {
				productsToFilter = new ArrayList<>();
			}
		}

		for (Product p : productsToFilter) {
			if (p.getName().toLowerCase().contains(query.toLowerCase())) {
				filteredList.add(p);
			}
		}

		adapter.notifyDataSetChanged();

		if (filteredList.isEmpty()) {
			Toast.makeText(this, "Không có sản phẩm nào phù hợp", Toast.LENGTH_SHORT).show();
		}
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