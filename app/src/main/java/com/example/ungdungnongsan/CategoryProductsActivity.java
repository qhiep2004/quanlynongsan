package com.example.ungdungnongsan;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private TextView tvCategoryName;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // Initialize views
        rvProducts = findViewById(R.id.rvCategoryProducts);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        btnBack = findViewById(R.id.btnBack);

        // Get category name from intent
        String categoryName = getIntent().getStringExtra("categoryName");
        tvCategoryName.setText(categoryName);

        // Set up RecyclerView
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(productAdapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Load products
        loadProductsByCategory(categoryName);
    }

    private void loadProductsByCategory(String categoryName) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("products")
                .child(categoryName);

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    String imageUrl = productSnapshot.child("imageUrl").getValue(String.class);
                    String name = productSnapshot.child("name").getValue(String.class);
                    String price = productSnapshot.child("price").getValue(String.class);
                    String description = productSnapshot.child("description").getValue(String.class);
                    String origin = productSnapshot.child("origin").getValue(String.class);
                    String ingredients = productSnapshot.child("ingredients").getValue(String.class);

                    if (name != null && imageUrl != null && price != null) {
                        Product product = new Product(name, imageUrl, price, description, origin, ingredients);
                        product.setKey(productSnapshot.getKey());
                        product.setCategory(categoryName);
                        productList.add(product);
                    }
                }

                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }
}