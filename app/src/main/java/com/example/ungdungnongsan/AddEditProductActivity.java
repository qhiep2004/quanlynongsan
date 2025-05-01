package com.example.ungdungnongsan;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditProductActivity extends AppCompatActivity {

	private EditText etName, etPrice, etDescription, etOrigin, etIngredients, etQuantity;
	private AutoCompleteTextView etCategory;
	private Button btnSave, btnChooseImage;
	private ImageView ivPreview;
	private Uri selectedImageUri;
	private Product product;
	private Cloudinary cloudinary;
	private static final String TAG = "AddEditProductActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_edit_product);

		// Kiểm tra quyền đọc bộ nhớ
		checkAndRequestPermissions();

		// Ánh xạ view
		etName = findViewById(R.id.etName);
		etPrice = findViewById(R.id.etPrice);
		etDescription = findViewById(R.id.etDescription);
		etOrigin = findViewById(R.id.etOrigin);
		etIngredients = findViewById(R.id.etIngredients);
		etCategory = findViewById(R.id.etCategory);
		etQuantity = findViewById(R.id.etQuantity);
		btnSave = findViewById(R.id.btnSave);
		btnChooseImage = findViewById(R.id.btnChooseImage);
		ivPreview = findViewById(R.id.ivPreview);

		// Cấu hình Cloudinary
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", "dcdynegor");
		config.put("api_key", "243559912455617");
		config.put("api_secret", "R63x1DBp-Ogkyd9L_9n65HkfW0o");
		cloudinary = new Cloudinary(config);

		// Thiết lập danh sách danh mục
		setupCategoryList();

		// Nếu truyền vào sản phẩm để sửa
		loadProductIfEditing();

		btnChooseImage.setOnClickListener(v -> chooseImage());
		btnSave.setOnClickListener(v -> {
			if (selectedImageUri != null) {
				uploadImageAndSave();
			} else if (product != null) {
				saveProduct(product.getImageUrl());
			} else {
				Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void checkAndRequestPermissions() {
		String[] permissions = {
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
		};

		List<String> permissionsToRequest = new ArrayList<>();

		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				permissionsToRequest.add(permission);
			}
		}

		if (!permissionsToRequest.isEmpty()) {
			ActivityCompat.requestPermissions(this,
					permissionsToRequest.toArray(new String[0]), 1);
		}
	}

	private void setupCategoryList() {
		List<String> categoryList = new ArrayList<>();
		categoryList.add("Cá");
		categoryList.add("Rau");
		categoryList.add("Thịt");
		categoryList.add("Trái cây");
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_dropdown_item_1line, categoryList);
		etCategory.setAdapter(adapter);
		etCategory.setThreshold(1);
	}

	private void loadProductIfEditing() {
		if (getIntent().hasExtra("product")) {
			product = (Product) getIntent().getSerializableExtra("product");
			etName.setText(product.getName());
			etPrice.setText(product.getPrice());
			etDescription.setText(product.getDescription());
			etOrigin.setText(product.getOrigin());
			etIngredients.setText(product.getIngredients());
			etCategory.setText(product.getCategory(), false);
			etQuantity.setText(String.valueOf(product.getQuantity()));


		}
	}

	
	private final ActivityResultLauncher<Intent> imagePickerLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == RESULT_OK && result.getData() != null) {
							selectedImageUri = result.getData().getData();
							Log.d(TAG, "Selected image URI: " + selectedImageUri);
							ivPreview.setImageURI(selectedImageUri);
						}
					});

	private void chooseImage() {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			imagePickerLauncher.launch(intent);
		} catch (Exception e) {
			Log.e(TAG, "Error choosing image", e);
			Toast.makeText(this, "Lỗi khi chọn ảnh: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	// Upload ảnh lên Cloudinary
	private void uploadImageAndSave() {
		if (selectedImageUri == null) {
			Toast.makeText(this, "Không có ảnh được chọn", Toast.LENGTH_SHORT).show();
			return;
		}

		ProgressDialog dialog = ProgressDialog.show(this, "", "Đang tải ảnh lên...", true);
		Log.d(TAG, "Starting upload with URI: " + selectedImageUri);

		new Thread(() -> {
			try {
				// Thử phương pháp sử dụng InputStream thay vì File
				InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
				if (inputStream == null) {
					runOnUiThread(() -> {
						dialog.dismiss();
						Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_LONG).show();
					});
					return;
				}

				Log.d(TAG, "Uploading image using InputStream");
				Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
				String imageUrl = (String) uploadResult.get("secure_url");

				Log.d(TAG, "Upload successful. URL: " + imageUrl);
				runOnUiThread(() -> {
					dialog.dismiss();
					saveProduct(imageUrl);
				});
			} catch (Exception e) {
				Log.e(TAG, "Upload failed", e);
				final String errorMsg = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";

				runOnUiThread(() -> {
					dialog.dismiss();
					Toast.makeText(this, "Upload thất bại: " + errorMsg,
							Toast.LENGTH_LONG).show();
				});
			}
		}).start();
	}


	private void saveProduct(String imageUrl) {
		String name = etName.getText().toString().trim();
		String price = etPrice.getText().toString().trim();
		String description = etDescription.getText().toString().trim();
		String origin = etOrigin.getText().toString().trim();
		String ingredients = etIngredients.getText().toString().trim();
		String category = etCategory.getText().toString().trim();
		String quantityStr = etQuantity.getText().toString().trim();

		if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
			Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
			return;
		}

		int quantity;
		try {
			quantity = Integer.parseInt(quantityStr);
		} catch (NumberFormatException ex) {
			Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
			return;
		}

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(this, "Không xác định người dùng", Toast.LENGTH_SHORT).show();
			return;
		}

		String sellerId = user.getUid();
		DatabaseReference ref = FirebaseDatabase
				                        .getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                        .getReference("products");

		try {
			if (product == null || !product.getCategory().equals(category)) {

				if (product != null) {
					ref.child(product.getCategory()).child(product.getKey()).removeValue();
				}
				String key = ref.child(category).push().getKey();
				Product newProd = new Product(name, imageUrl, price, description, origin, ingredients);
				newProd.setKey(key);
				newProd.setCategory(category);
				newProd.setIdSeller(sellerId);
				newProd.setQuantity(quantity);
				ref.child(category).child(key).setValue(newProd);
			} else {

				product.setName(name);
				product.setPrice(price);
				product.setImageUrl(imageUrl);
				product.setDescription(description);
				product.setOrigin(origin);
				product.setIngredients(ingredients);
				product.setQuantity(quantity);
				ref.child(category).child(product.getKey()).setValue(product);
			}

			Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();
		} catch (Exception e) {
			Log.e(TAG, "Error saving product", e);
			Toast.makeText(this, "Lỗi khi lưu sản phẩm: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}
}