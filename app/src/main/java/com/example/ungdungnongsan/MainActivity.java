package com.example.ungdungnongsan;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.graphics.Rect;
import android.view.View;
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
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

	private RecyclerView rvGroupProducts;
	private GroupProductAdapter groupProductAdapter;
	private List<GroupProduct> groupProductList;
	private RecyclerView rvCategories;
	private CategoryAdapter categoryAdapter;
	private List<String> categoryList;
	private EditText etSearch;


	private boolean isDarkMode = false;
	private SharedPreferences themePrefs;


	private SensorManager sensorManager;
	private Sensor lightSensor;
	private boolean isAutoTheme = false;
	private static final float LIGHT_THRESHOLD = 50f;
	private Switch autoThemeSwitch;

	private boolean isProcessingThemeChange = false;

	private Handler themeChangeHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
		isDarkMode = themePrefs.getBoolean("isDarkMode", false);
		isAutoTheme = themePrefs.getBoolean("isAutoTheme", false);


		if (isDarkMode) {
			setTheme(R.style.AppTheme_Dark);
		} else {
			setTheme(R.style.AppTheme1);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		initLightSensor();


		setupAutoThemeSwitch();

		ImageView ivThemeToggle = findViewById(R.id.ivThemeToggle);
		ivThemeToggle.setOnClickListener(v -> toggleTheme());

		findViewById(R.id.ivCart).setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, CartActivity.class);
			startActivity(intent);
		});

		etSearch = findViewById(R.id.etSearch);
		ImageView ivUserIcon = findViewById(R.id.ivUserIcon);
		ivUserIcon.setOnClickListener(v -> {
			SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
			String email = prefs.getString("email", "Không có dữ liệu");
			String name = prefs.getString("name", "Không có dữ liệu");
			String phone = prefs.getString("phone", "Không có dữ liệu");
			String address = prefs.getString("address", "Không có dữ liệu");

			String message = "Email: " + email + "\n"
					                 + "Họ tên: " + name + "\n"
					                 + "SĐT: " + phone + "\n"
					                 + "Địa chỉ: " + address;

			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Thông tin người dùng");
			builder.setMessage(message);
			builder.setPositiveButton("OK", null);
			builder.setNegativeButton("Chỉnh sửa", (dialog, which) -> {
				startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
			});
			builder.setNeutralButton("Đăng xuất", (dialog, which) -> {
				SharedPreferences.Editor editor = prefs.edit();
				editor.clear();
				editor.apply();
				Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MainActivity.this, LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
			});
			builder.show();
		});

		etSearch.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				filterProductsByName(s.toString());
			}
			@Override public void afterTextChanged(Editable s) {}
		});

		setupCategoryRecyclerView();
		setupProductsRecyclerView();
		fetchDataFromFirebase();
	}

	private void initLightSensor() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			if (lightSensor == null) {
				Log.e("LightSensor", "Thiết bị không hỗ trợ cảm biến ánh sáng");
				Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến ánh sáng", Toast.LENGTH_SHORT).show();


				isAutoTheme = false;
				SharedPreferences.Editor editor = themePrefs.edit();
				editor.putBoolean("isAutoTheme", false);
				editor.apply();
			} else {

				Log.d("LightSensor", "Cảm biến ánh sáng: " + lightSensor.getName());
				Log.d("LightSensor", "Nhà sản xuất: " + lightSensor.getVendor());
				Log.d("LightSensor", "Phạm vi tối đa: " + lightSensor.getMaximumRange());
				Log.d("LightSensor", "Độ phân giải: " + lightSensor.getResolution());
				Log.d("LightSensor", "Mức tiêu thụ năng lượng: " + lightSensor.getPower() + " mA");
			}
		} else {
			Log.e("LightSensor", "Không thể khởi tạo SensorManager");
		}
	}

	private void setupAutoThemeSwitch() {
		autoThemeSwitch = findViewById(R.id.autoThemeSwitch);

		// Ẩn switch nếu không có cảm biến ánh sáng
		if (lightSensor == null) {
			autoThemeSwitch.setVisibility(View.GONE);
		} else {
			autoThemeSwitch.setChecked(isAutoTheme);
			autoThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isAutoTheme = isChecked;
					SharedPreferences.Editor editor = themePrefs.edit();
					editor.putBoolean("isAutoTheme", isAutoTheme);
					editor.apply();

					if (isAutoTheme) {
						Toast.makeText(MainActivity.this, "Đã bật chế độ tự động điều chỉnh theme", Toast.LENGTH_SHORT).show();
						registerLightSensor();
					} else {
						Toast.makeText(MainActivity.this, "Đã tắt chế độ tự động điều chỉnh theme", Toast.LENGTH_SHORT).show();
						unregisterLightSensor();
					}
				}
			});
		}
	}

	private void setupCategoryRecyclerView() {
		rvCategories = findViewById(R.id.rvCategories);
		rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		categoryList = new ArrayList<>();
		categoryList.add("Tất cả");
		categoryList.add("Cá");
		categoryList.add("Rau");
		categoryList.add("Thịt");
		categoryList.add("Trái cây");

		categoryAdapter = new CategoryAdapter(categoryList, categoryName -> {
			if (categoryName.equals("Tất cả")) {
				groupProductAdapter.setData(groupProductList);
				return;
			}
			List<GroupProduct> filteredGroups = new ArrayList<>();
			for (GroupProduct group : groupProductList) {
				if (group.getGroupName().equalsIgnoreCase(categoryName)) {
					filteredGroups.add(group);
					break;
				}
			}
			groupProductAdapter.setData(filteredGroups);
		});
		rvCategories.setAdapter(categoryAdapter);
	}

	private void setupProductsRecyclerView() {
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isAutoTheme && lightSensor != null) {
			registerLightSensor();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterLightSensor();

		themeChangeHandler.removeCallbacksAndMessages(null);
	}

	private void registerLightSensor() {
		if (sensorManager != null && lightSensor != null) {

			boolean success = sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);

			if (success) {
				Log.d("LightSensor", "Đã đăng ký cảm biến ánh sáng thành công");
			} else {
				Log.e("LightSensor", "Không thể đăng ký cảm biến ánh sáng");
				Toast.makeText(this, "Không thể kích hoạt cảm biến ánh sáng", Toast.LENGTH_SHORT).show();


				isAutoTheme = false;
				autoThemeSwitch.setChecked(false);
				SharedPreferences.Editor editor = themePrefs.edit();
				editor.putBoolean("isAutoTheme", false);
				editor.apply();
			}
		} else {
			Log.e("LightSensor", "SensorManager hoặc LightSensor là null khi cố gắng đăng ký");
		}
	}

	private void unregisterLightSensor() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
			Log.d("LightSensor", "Đã hủy đăng ký cảm biến ánh sáng");
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT && isAutoTheme) {
			float lightValue = event.values[0];
			Log.d("LightSensor", "Giá trị ánh sáng: " + lightValue);

			boolean shouldBeDark = lightValue < LIGHT_THRESHOLD;


			if (isDarkMode != shouldBeDark && !isProcessingThemeChange) {
				isDarkMode = shouldBeDark;


				isProcessingThemeChange = true;


				SharedPreferences.Editor editor = themePrefs.edit();
				editor.putBoolean("isDarkMode", isDarkMode);
				editor.apply();


				themeChangeHandler.removeCallbacksAndMessages(null);
				themeChangeHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								Toast.makeText(MainActivity.this,
										isDarkMode ? "Đã phát hiện ánh sáng yếu, chuyển sang chế độ tối" :
												"Đã phát hiện ánh sáng đủ, chuyển sang chế độ sáng",
										Toast.LENGTH_SHORT).show();


								recreate();


								themeChangeHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										isProcessingThemeChange = false;
									}
								}, 2000);
							}
						});
					}
				}, 500); // 500ms delay trước khi thực sự thay đổi theme
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Ghi log khi độ chính xác thay đổi
		if (sensor.getType() == Sensor.TYPE_LIGHT) {
			Log.d("LightSensor", "Độ chính xác cảm biến ánh sáng thay đổi: " + accuracy);
		}
	}

	private void toggleTheme() {
		if (isAutoTheme) {
			Toast.makeText(this, "Vui lòng tắt chế độ tự động trước khi thay đổi theme thủ công", Toast.LENGTH_SHORT).show();
			return;
		}

		isDarkMode = !isDarkMode;

		// Lưu trạng thái mới
		SharedPreferences.Editor editor = themePrefs.edit();
		editor.putBoolean("isDarkMode", isDarkMode);
		editor.apply();

		// Thông báo cho người dùng
		Toast.makeText(this, isDarkMode ? "Đã chuyển sang chế độ tối" : "Đã chuyển sang chế độ sáng",
				Toast.LENGTH_SHORT).show();

		// Khởi động lại activity để áp dụng theme mới
		recreate();
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
						int quantity = idSnapshot.child("quantity").getValue(Integer.class) != null
								               ? idSnapshot.child("quantity").getValue(Integer.class)
								               : 0;
						if (name != null && imageUrl != null && price != null ) {
							Product product = new Product(name, imageUrl, price, description, origin, ingredients);
							product.setKey(idSnapshot.getKey());
							product.setQuantity(quantity);
							product.setCategory(groupName);
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