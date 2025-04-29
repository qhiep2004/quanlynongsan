package com.example.ungdungnongsan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

	private static final String TAG = "StatisticsActivity";
	private TextView tvTotalRevenue, tvTotalProductsSold;
	private RecyclerView rvProductStats;
	private ProductStatsAdapter productStatsAdapter;
	private List<ProductStats> productStatsList;
	private DecimalFormat decimalFormat = new DecimalFormat("#,###");


	private static final String FIREBASE_URL = "https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app";


	private FirebaseDatabase database;
	private String currentSellerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);


		database = FirebaseDatabase.getInstance(FIREBASE_URL);


		tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
		tvTotalProductsSold = findViewById(R.id.tvTotalProductsSold);
		rvProductStats = findViewById(R.id.rvProductStats);


		rvProductStats.setLayoutManager(new LinearLayoutManager(this));
		productStatsList = new ArrayList<>();
		productStatsAdapter = new ProductStatsAdapter(productStatsList);
		rvProductStats.setAdapter(productStatsAdapter);


		tvTotalProductsSold.setText("Tổng số sản phẩm đã bán: 0");
		tvTotalRevenue.setText("Tổng doanh thu: 0đ");


		loadStatistics();
	}

	private void loadStatistics() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
			return;
		}
		currentSellerId = user.getUid();


		Log.d(TAG, "Bắt đầu tải dữ liệu với sellerId: " + currentSellerId);

		try {

			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Đang tải dữ liệu...");
			progressDialog.setCancelable(false);
			progressDialog.show();


			DatabaseReference ordersRef = database.getReference("orders");

			ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					progressDialog.dismiss();

					Log.d(TAG, "Nhận được dữ liệu từ Firebase: " + snapshot.exists() + ", Count: " + snapshot.getChildrenCount());

					if (!snapshot.exists()) {
						Toast.makeText(StatisticsActivity.this, "Không có dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
						return;
					}

					Map<String, ProductStats> statsMap = new HashMap<>();
					int totalProductsSold = 0;
					int totalRevenue = 0;

					for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
						try {

							Log.d(TAG, "Order key: " + orderSnapshot.getKey());


							for (DataSnapshot child : orderSnapshot.getChildren()) {
								Log.d(TAG, "  Field: " + child.getKey() + " = " + child.getValue());
							}


							if (orderSnapshot.hasChild("cartItems")) {
								DataSnapshot cartItemsSnapshot = orderSnapshot.child("cartItems");
								int orderTotal = 0;
								boolean hasSellerProducts = false;

								for (DataSnapshot cartItemSnapshot : cartItemsSnapshot.getChildren()) {

									DataSnapshot productSnapshot = cartItemSnapshot.child("product");
									String productSellerId = productSnapshot.child("idSeller").getValue(String.class);


									if (productSellerId != null && productSellerId.equals(currentSellerId)) {
										hasSellerProducts = true;


										String productName = productSnapshot.child("name").getValue(String.class);
										int quantity = cartItemSnapshot.child("quantity").getValue(Integer.class);


										Object priceObj = productSnapshot.child("price").getValue();
										int price = 0;

										if (priceObj instanceof String) {

											String priceStr = (String) priceObj;
											priceStr = priceStr.replace(".", "");
											price = Integer.parseInt(priceStr);
										} else if (priceObj instanceof Long || priceObj instanceof Integer) {

											price = ((Number) priceObj).intValue();
										}

										int itemRevenue = price * quantity;
										orderTotal += itemRevenue;


										ProductStats stats = statsMap.containsKey(productName)
												                     ? statsMap.get(productName)
												                     : new ProductStats(productName);
										stats.addSales(quantity, itemRevenue);
										statsMap.put(productName, stats);


										totalProductsSold += quantity;

										Log.d(TAG, "Đã xử lý sản phẩm của seller: " + productName +
												           ", SL: " + quantity + ", Giá: " + price +
												           ", Doanh thu: " + itemRevenue);
									}
								}

								if (hasSellerProducts) {

									totalRevenue += orderTotal;
									Log.d(TAG, "Doanh thu từ đơn hàng: " + orderTotal);
								}
							}
						} catch (Exception e) {
							Log.e(TAG, "Lỗi xử lý đơn hàng: " + e.getMessage(), e);
						}
					}

					Log.d(TAG, "Tổng kết: " + totalProductsSold + " sản phẩm, " + totalRevenue + "đ");


					updateUI(statsMap, totalProductsSold, totalRevenue);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError error) {
					progressDialog.dismiss();

					Log.e(TAG, "Lỗi Firebase: " + error.getMessage());
					Toast.makeText(StatisticsActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "Lỗi kết nối đến Firebase: " + e.getMessage(), e);
			Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}


	private void updateUI(Map<String, ProductStats> statsMap, int totalProductsSold, int totalRevenue) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					tvTotalProductsSold.setText("Tổng số sản phẩm đã bán: " + totalProductsSold);
					tvTotalRevenue.setText("Tổng doanh thu: " + decimalFormat.format(totalRevenue) + "đ");


					productStatsList.clear();
					productStatsList.addAll(statsMap.values());
					productStatsAdapter.notifyDataSetChanged();

					Log.d(TAG, "UI đã được cập nhật thành công");
				} catch (Exception e) {
					Log.e(TAG, "Lỗi khi cập nhật UI: " + e.getMessage(), e);
				}
			}
		});
	}
}