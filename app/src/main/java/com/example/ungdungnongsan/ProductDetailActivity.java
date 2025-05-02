package com.example.ungdungnongsan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

	private TextView tvName, tvPrice, tvDescription, tvOrigin, tvIngredients;
	private TextView tvQuantity;
	private ImageButton btnIncrease, btnDecrease;
	private ImageView ivProduct;
	private int quantity = 1;
	private Product currentProduct;
	private RecyclerView rvSimilarProducts;
	private Button btnVerifyOrigin;

	@SuppressLint("MissingInflatedId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_detail);

		// Set up the toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		findViewById(R.id.ivCart).setOnClickListener(v -> {
			Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
			startActivity(intent);
		});

		// Initialize views
		initializeViews();
		setupQuantityControls();

		// Set up similar products RecyclerView
		rvSimilarProducts = findViewById(R.id.rvSimilarProducts);
		rvSimilarProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

		btnVerifyOrigin = findViewById(R.id.btnVerifyOrigin);
		btnVerifyOrigin.setOnClickListener(v -> {
			if (currentProduct != null) {
				Intent intent = new Intent(ProductDetailActivity.this, ProductOriginActivity.class);
				intent.putExtra("product", currentProduct);
				startActivity(intent);
			} else {
				Toast.makeText(ProductDetailActivity.this, "Không có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
			}
		});

		// Get product from intent
		currentProduct = (Product) getIntent().getSerializableExtra("product");
		String productId = getIntent().getStringExtra("productId");
		String category = getIntent().getStringExtra("category");

		if (currentProduct != null) {
			displayProductDetails();
		} else if (productId != null && category != null) {
			loadProductDetails(productId, category);
		} else {
			Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
			finish();
		}

		setupAddToCartButton();
	}

	private void initializeViews() {
		tvName = findViewById(R.id.tvName);
		tvPrice = findViewById(R.id.tvPrice);
		tvDescription = findViewById(R.id.tvDescription);
		tvOrigin = findViewById(R.id.tvOrigin);
		tvIngredients = findViewById(R.id.tvIngredients);
		ivProduct = findViewById(R.id.ivProduct);

		// Initialize quantity controls
		tvQuantity = findViewById(R.id.tvQuantity);
		btnIncrease = findViewById(R.id.btnIncrease);
		btnDecrease = findViewById(R.id.btnDecrease);
	}

	private void setupQuantityControls() {
		btnIncrease.setOnClickListener(v -> {
			quantity++;
			tvQuantity.setText(String.valueOf(quantity));
		});

		btnDecrease.setOnClickListener(v -> {
			if (quantity > 1) {
				quantity--;
				tvQuantity.setText(String.valueOf(quantity));
			}
		});
	}

	private void loadSimilarProducts(String category) {
		if (category == null) {
			return;
		}

		DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				.getReference("products").child(category);

		databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				List<Product> similarProducts = new ArrayList<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					try {
						Product product = snapshot.getValue(Product.class);
						if (product != null && currentProduct != null &&
								currentProduct.getKey() != null && product.getKey() != null &&
								!product.getKey().equals(currentProduct.getKey())) {
							similarProducts.add(product);
						}
					} catch (Exception e) {
						Log.e("ProductDetail", "Error loading similar product: " + e.getMessage());
					}
				}
				ProductAdapter adapter = new ProductAdapter(ProductDetailActivity.this, similarProducts);
				rvSimilarProducts.setAdapter(adapter);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ProductDetailActivity.this, "Failed to load similar products", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void loadProductDetails(String productId, String category) {
		if (productId == null || category == null) {
			Toast.makeText(this, "Không đủ thông tin sản phẩm", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		DatabaseReference productRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				.getReference("products").child(category).child(productId);

		productRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				try {
					if (dataSnapshot.exists()) {
						String name = dataSnapshot.child("name").getValue(String.class);
						String price = dataSnapshot.child("price").getValue(String.class);
						String description = dataSnapshot.child("description").getValue(String.class);
						String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
						String origin = dataSnapshot.child("origin").getValue(String.class);
						String ingredients = dataSnapshot.child("ingredients").getValue(String.class);

						currentProduct = new Product(name, imageUrl, price, description, origin, ingredients);
						currentProduct.setCategory(category);
						currentProduct.setKey(productId);

						// Load certificate data
						if (dataSnapshot.hasChild("certificate")) {
							try {
								// Check if certificate is actually an object and not a primitive value
								Object certObj = dataSnapshot.child("certificate").getValue();
								if (certObj instanceof Long || certObj instanceof Integer || certObj instanceof Double) {
									// Certificate is a number, not an object - skip it
									Log.d("ProductDetail", "Certificate is a numeric value, not an object");
								} else {
									DataSnapshot certSnapshot = dataSnapshot.child("certificate");
									if (certSnapshot.exists() && certSnapshot.hasChildren()) {
										for (DataSnapshot certChild : certSnapshot.getChildren()) {
											String certName = certChild.child("name").getValue(String.class);
											String organization = certChild.child("organization").getValue(String.class);
											String issueDate = certChild.child("issuedDate").getValue(String.class); // chú ý: issuedDate chứ không phải issueDate
											String expiryDate = certChild.child("expiryDate").getValue(String.class);
											String imageURL = certChild.child("imageUrl").getValue(String.class); // chú ý: imageUrl viết thường `u`

											Certificate certificate = new Certificate(certName, organization, issueDate, expiryDate, imageURL);
											currentProduct.setCertificate(certificate);
											break; // chỉ lấy chứng nhận đầu tiên
										}
									}
								}
							} catch (Exception e) {
								Log.e("ProductDetail", "Error loading certificate: " + e.getMessage());
								e.printStackTrace();
							}
						}

						displayProductDetails();
					} else {
						Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
						finish();
					}
				} catch (Exception e) {
					Log.e("ProductDetail", "Error in onDataChange: " + e.getMessage());
					Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					finish();
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Toast.makeText(ProductDetailActivity.this,
						"Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	private void displayProductDetails() {
		if (currentProduct != null) {
			tvName.setText(currentProduct.getName());
			tvPrice.setText(currentProduct.getPrice());
			tvDescription.setText(currentProduct.getDescription());

			// Format origin with location icon
			String origin = currentProduct.getOrigin() != null ? currentProduct.getOrigin() : "Không rõ nguồn gốc";
			tvOrigin.setText(origin);

			tvIngredients.setText(currentProduct.getIngredients());

			if (currentProduct.getImageUrl() != null) {
				Glide.with(this).load(currentProduct.getImageUrl())
						.placeholder(R.drawable.ic_launcher_background)
						.error(R.drawable.ic_launcher_background)
						.into(ivProduct);
			}

			// Enable the certificate verification button only if there's a certificate
			if (currentProduct.getCertificate() != null) {
				btnVerifyOrigin.setEnabled(true);
				btnVerifyOrigin.setAlpha(1.0f);
			} else {
				btnVerifyOrigin.setEnabled(false);
				btnVerifyOrigin.setAlpha(0.5f);
			}

			// Load similar products
			loadSimilarProducts(currentProduct.getCategory());
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	private void setupAddToCartButton() {
		findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
			if (currentProduct != null) {
				for (int i = 0; i < quantity; i++) {
					CartManager.getInstance().addToCart(currentProduct);
				}
				Toast.makeText(this, "Đã thêm " + quantity + " sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Không thể thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show();
			}
		});
	}
}