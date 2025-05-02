package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private RecyclerView rvCart, rvRelatedProducts;
    private CartAdapter cartAdapter;
    private ProductAdapter relatedProductsAdapter;
    private TextView tvTotal, tvCartCount;
    private ArrayList<CartItem> cartItems;
    private ArrayList<Product> relatedProducts;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        decimalFormat = new DecimalFormat("#,###");
        initViews();
        setupCart();
        setupRelatedProducts();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        rvRelatedProducts = findViewById(R.id.rvRelatedProducts);
        tvTotal = findViewById(R.id.tvTotal);
        tvCartCount = findViewById(R.id.tvCartCount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> startCheckout());
    }

    private void setupCart() {
        cartItems = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);
        updateCartInfo();
    }

    private void setupRelatedProducts() {
        relatedProducts = new ArrayList<>();
        relatedProductsAdapter = new ProductAdapter(this, relatedProducts);
        rvRelatedProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelatedProducts.setAdapter(relatedProductsAdapter);

        // Get category of first item in cart
        if (!cartItems.isEmpty()) {
            String category = cartItems.get(0).getProduct().getCategory();
            // Fetch related products from Firebase based on category
            DatabaseReference rootRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("products").child(category);
            rootRef.limitToFirst(5).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    relatedProducts.clear();
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
                            product.setCategory(category);
                            relatedProducts.add(product);
                        }
                    }
                    relatedProductsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CartActivity.this, "Lỗi tải sản phẩm liên quan", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateCartInfo() {
        tvCartCount.setText("Giỏ hàng (" + CartManager.getInstance().getItemCount() + ")");
        updateTotal();
    }

    private void updateTotal() {
        double total = calculateTotalAmount();
        tvTotal.setText("Tổng tiền: " + decimalFormat.format(total) + "đ");
    }

    @Override
    public void onQuantityChanged() {
        updateCartInfo();
    }

    @Override
    public void onItemRemoved() {
        updateCartInfo();
        if (cartItems.isEmpty()) {
            finish();
        }
    }


    private void startCheckout() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("cartItems", cartItems);
            intent.putExtra("totalAmount", calculateTotalAmount());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private double calculateTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            String cleanPrice = item.getProduct().getPrice().replace(".", "");
            total += Double.parseDouble(cleanPrice) * item.getQuantity();
        }
        return total;
    }
}