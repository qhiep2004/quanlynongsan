package com.example.ungdungnongsan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "MainActivity";

    private RecyclerView rvCategories;
    private RecyclerView rvGroupProducts;
    private CategoryAdapter categoryAdapter;
    private GroupProductAdapter groupProductAdapter;
    private ImageView ivCart, ivUserIcon;
    private EditText etSearch;

    private List<String> categories = new ArrayList<>();
    private List<GroupProduct> allGroupProducts = new ArrayList<>();
    private List<GroupProduct> filteredGroupProducts = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            // Not signed in, redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views and continue with normal flow
        initViews();
        setupRecyclerViews();
        loadCategories();
        loadAllProducts();
        setupListeners();
    }

    // Initialize views from layout
    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        rvGroupProducts = findViewById(R.id.rvGroupProducts);
        ivCart = findViewById(R.id.ivCart);
        ivUserIcon = findViewById(R.id.ivUserIcon);
        etSearch = findViewById(R.id.etSearch);
    }

    // Set up RecyclerViews with their adapters
    private void setupRecyclerViews() {
        // Categories horizontal list
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categories = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categories, this);
        rvCategories.setAdapter(categoryAdapter);

        // Group products vertical list
        rvGroupProducts.setLayoutManager(new LinearLayoutManager(this));
        allGroupProducts = new ArrayList<>();
        filteredGroupProducts = new ArrayList<>();
        groupProductAdapter = new GroupProductAdapter(this, filteredGroupProducts);
        rvGroupProducts.setAdapter(groupProductAdapter);
    }

    // Set up click listeners and other interactions
    private void setupListeners() {
        ivCart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ivUserIcon.setOnClickListener(v -> {
            // Display user information in an alert dialog
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String email = prefs.getString("email", "Không có dữ liệu");
            String name = prefs.getString("name", "Không có dữ liệu");
            String phone = prefs.getString("phone", "Không có dữ liệu");
            String address = prefs.getString("address", "Không có dữ liệu");

            String message = "Email: " + email + "\n"
                    + "Họ tên: " + name + "\n"
                    + "SĐT: " + phone + "\n"
                    + "Địa chỉ: " + address;

            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Thông tin người dùng")
                    .setMessage(message)
                    .setPositiveButton("Đóng", null)
                    .setNegativeButton("Đăng xuất", (dialog, which) -> {
                        mAuth.signOut();
                        redirectToLogin();
                    })
                    .show();
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString());
            }
        });
    }

    private void redirectToLogin() {
        // Safely redirect to login on any critical failure
        try {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Even redirect to login failed", e);
            finish(); // Last resort, just close the app
        }
    }

    // Load categories from Firebase
    private void loadCategories() {
        categories.clear();
        
        // Always add "Tất cả" (All) as the first category
        categories.add("Tất cả");
        
        // Then add other categories
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("products");
                
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    if (categoryName != null && !categoryName.equals("Tất cả")) {
                        categories.add(categoryName);
                    }
                }
                
                categoryAdapter.notifyDataSetChanged();
                
                // After loading categories, we can set category counts
                if (allGroupProducts.size() > 0) {
                    categoryAdapter.setCategoryCounts(allGroupProducts);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading categories: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load all product data
    private void loadAllProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("products");
                
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allGroupProducts.clear();
                
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    List<Product> products = new ArrayList<>();
                    
                    for (DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                        Product product = new Product();
                        product.setKey(productSnapshot.getKey());
                        product.setCategory(categoryName);
                        product.setName(productSnapshot.child("name").getValue(String.class));
                        product.setImageUrl(productSnapshot.child("imageUrl").getValue(String.class));
                        product.setPrice(productSnapshot.child("price").getValue(String.class));
                        product.setDescription(productSnapshot.child("description").getValue(String.class));
                        product.setOrigin(productSnapshot.child("origin").getValue(String.class));
                        product.setIngredients(productSnapshot.child("ingredients").getValue(String.class));
                        product.setIdSeller(productSnapshot.child("idSeller").getValue(String.class));
                        
                        products.add(product);
                    }
                    
                    if (!products.isEmpty()) {
                        allGroupProducts.add(new GroupProduct(categoryName, products));
                    }
                }
                
                // Initially show all products
                filterByCategory("Tất cả");
                
                // Update category counts
                categoryAdapter.setCategoryCounts(allGroupProducts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading products: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter products based on search text
    private void filterProducts(String query) {
        // First apply category filter
        String selectedCategory = categories.get(categoryAdapter.getSelectedPosition());
        
        // Then apply search filter
        if (query.isEmpty()) {
            // If no search query, just filter by category
            filterByCategory(selectedCategory);
        } else {
            // Filter by both category and search query
            filteredGroupProducts.clear();
            
            for (GroupProduct group : allGroupProducts) {
                // Skip if not in selected category and not "All"
                if (!selectedCategory.equals("Tất cả") && !group.getGroupName().equals(selectedCategory)) {
                    continue;
                }
                
                // Filter products in this group by search query
                List<Product> matchedProducts = new ArrayList<>();
                for (Product product : group.getProducts()) {
                    if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                        matchedProducts.add(product);
                    }
                }
                
                // Add group if it has matching products
                if (!matchedProducts.isEmpty()) {
                    filteredGroupProducts.add(new GroupProduct(group.getGroupName(), matchedProducts));
                }
            }
            
            groupProductAdapter.setData(filteredGroupProducts);
        }
    }

    // Filter products based on selected category
    private void filterByCategory(String categoryName) {
        filteredGroupProducts.clear();
        
        if (categoryName.equals("Tất cả")) {
            // Show all groups
            filteredGroupProducts.addAll(allGroupProducts);
        } else {
            // Show only the selected category
            for (GroupProduct group : allGroupProducts) {
                if (group.getGroupName().equals(categoryName)) {
                    filteredGroupProducts.add(group);
                    break;
                }
            }
        }
        
        groupProductAdapter.setData(filteredGroupProducts);
    }

    // Implementation of CategoryAdapter.OnCategoryClickListener interface
    @Override
    public void onCategoryClick(String categoryName) {
        // Filter products based on selected category
        filterByCategory(categoryName);
        
        // Also reset search to avoid confusion
        etSearch.setText("");
    }
}
