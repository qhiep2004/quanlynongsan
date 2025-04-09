package com.example.ungdungnongsan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import	android.graphics.Rect;
import	android.view.View;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;

public class MainActivity extends AppCompatActivity {

	private RecyclerView rvGroupProducts;
	private GroupProductAdapter groupProductAdapter;
	private List<GroupProduct> groupProductList;
	private RecyclerView rvCategories;
	private CategoryAdapter categoryAdapter;
	private List<String> categoryList;
	private EditText etSearch;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		etSearch = findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				filterProductsByName(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});


		rvCategories = findViewById(R.id.rvCategories);
		rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

		categoryList = new ArrayList<>();
		categoryList.add("Tất cả");
		categoryList.add("Cá");
		categoryList.add("Rau");
		categoryList.add("Thịt");
		categoryList.add("Trái cây");

		categoryAdapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryClickListener() {
			@Override
			public void onCategoryClick(String categoryName) {
				Toast.makeText(MainActivity.this, "Chọn: " + categoryName, Toast.LENGTH_SHORT).show();

			}
		});
		rvCategories.setAdapter(categoryAdapter);

		rvGroupProducts = findViewById(R.id.rvGroupProducts);
		rvGroupProducts.setLayoutManager(new LinearLayoutManager(this));

		int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.group_spacing);
		rvGroupProducts.addItemDecoration(new RecyclerView.ItemDecoration() {
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				outRect.bottom = spacingInPixels;
			}
		});

		groupProductList = new ArrayList<>();
		groupProductAdapter = new GroupProductAdapter(this, groupProductList);
		rvGroupProducts.setAdapter(groupProductAdapter);

		fetchDataFromFirebase();
	}


	private void fetchDataFromFirebase() {
		DatabaseReference rootRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                            .getReference("products");

		Log.d("MainActivity", "Bắt đầu tải dữ liệu từ Firebase");

		rootRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.d("MainActivity", "Đã nhận dữ liệu: " + snapshot.getChildrenCount() + " nhóm");
				groupProductList.clear();

				for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
					String groupName = groupSnapshot.getKey();
					List<Product> productList = new ArrayList<>();

					Log.d("MainActivity", "Đang xử lý nhóm: " + groupName);

					for (DataSnapshot idSnapshot : groupSnapshot.getChildren()) {
						String imageUrl = idSnapshot.child("imageUrl").getValue(String.class);
						String name = idSnapshot.child("name").getValue(String.class);
						String price = idSnapshot.child("price").getValue(String.class);
						String description = idSnapshot.child("description").getValue(String.class);
						String origin = idSnapshot.child("origin").getValue(String.class);
						String ingredients = idSnapshot.child("ingredients").getValue(String.class);

						if (name != null && imageUrl != null && price != null) {
							Product product = new Product(name, imageUrl, price, description, origin, ingredients);
							productList.add(product);
							Log.d("MainActivity", "Đã thêm sản phẩm: " + name);
						} else {
							Log.e("MainActivity", "Thiếu thông tin sản phẩm cho " + idSnapshot.getKey());
						}
					}

					groupProductList.add(new GroupProduct(groupName, productList));
				}

				groupProductAdapter.setData(groupProductList);
				Log.d("MainActivity", "Cập nhật adapter với " + groupProductList.size() + " nhóm");
			}




	@Override
			public void onCancelled(@NonNull DatabaseError error) {
				Log.e("MainActivity", "Lỗi Firebase: " + error.getMessage());
				Toast.makeText(MainActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});

	}

	private void filterProductsByName(String keyword) {
		if (keyword.isEmpty()) {
			groupProductAdapter.setData(groupProductList);
			return;
		}

		List<GroupProduct> filteredGroups = new ArrayList<>();

		for (GroupProduct group : groupProductList) {
			List<Product> matchedProducts = new ArrayList<>();
			for (Product product : group.getProducts()) {
				if (product.getName().toLowerCase().contains(keyword.toLowerCase())) {
					matchedProducts.add(product);
				}
			}
			if (!matchedProducts.isEmpty()) {
				filteredGroups.add(new GroupProduct(group.getGroupName(), matchedProducts));
			}
		}

		groupProductAdapter.setData(filteredGroups);
	}
}


