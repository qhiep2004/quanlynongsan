package com.example.ungdungnongsan;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.util.Log;
public class CheckoutActivity extends AppCompatActivity implements OnMapReadyCallback, LocationHelper.LocationListener {
    private TextView tvOrderSummary, tvTotalAmount;
    private EditText etName, etAddress, etPhone;
    private Button btnPlaceOrder;
    private ArrayList<CartItem> cartItems;
    private double totalAmount;
    private LocationHelper locationHelper;
    private TextView tvLocation;
    private Button btnGetLocation;
    private double latitude, longitude;
    private GoogleMap mMap;
    private DecimalFormat decimalFormat;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);


        decimalFormat = new DecimalFormat("#,###");

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                                                      .findFragmentById(R.id.map_checkout);
        mapFragment.getMapAsync(this);

        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        try {

            cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
            totalAmount = getIntent().getDoubleExtra("totalAmount", 0);

            if (cartItems == null) {
                cartItems = new ArrayList<>();
                Toast.makeText(this, "Không có sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải thông tin đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            cartItems = new ArrayList<>();
        }
        displayOrderSummary();
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        locationHelper = new LocationHelper(this, this);

        btnGetLocation.setOnClickListener(v -> {
            locationHelper.getCurrentLocation(new LocationHelper.SingleLocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    tvLocation.setText("Vị trí: " + latitude + ", " + longitude);


                    getAddressFromLocation(location);


                    updateMapLocation(location);
                }

                @Override
                public void onLocationFailed(String reason) {
                    Toast.makeText(CheckoutActivity.this, "Lỗi: " + reason, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateMapLocation(Location location) {
        if (mMap == null) return;

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());


        mMap.clear();


        mMap.addMarker(new MarkerOptions()
                               .position(userLocation)
                               .title("Vị trí của bạn"));


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng vietnam = new LatLng(16.0, 108.0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vietnam, 5));
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }

                String addressStr = sb.toString();
                etAddress.setText(addressStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayOrderSummary() {
        StringBuilder summary = new StringBuilder();
        for (CartItem item : cartItems) {

            String cleanPrice = item.getProduct().getPrice().replace(".", "");
            double itemTotal = Double.parseDouble(cleanPrice) * item.getQuantity();

            summary.append(item.getProduct().getName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" = ")
                    .append(decimalFormat.format(itemTotal))
                    .append("đ\n");
        }
        tvOrderSummary.setText(summary.toString());
        tvTotalAmount.setText("Tổng tiền: " + decimalFormat.format(totalAmount) + "đ");
    }

    private void placeOrder() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }


        DatabaseReference ordersRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                                              .getReference("orders");
        String orderId = ordersRef.push().getKey();

        Order order = new Order(orderId, name, address, phone, cartItems, totalAmount);
        order.setLatitude(latitude);
        order.setLongitude(longitude);
        ordersRef.child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    for (CartItem item : cartItems) {
                        Product product = item.getProduct();
                        int quantityPurchased = item.getQuantity();
                        String productCategory = product.getCategory();

                        // Sử dụng ID là thuộc tính chính
                        String productKey = product.getId();

                        // Kiểm tra null và thử sử dụng key nếu id là null
                        if (productKey == null) {
                            productKey = product.getKey();
                        }

                        // Kiểm tra lại nếu vẫn null
                        if (productKey == null || productKey.isEmpty()) {
                            Log.e("CheckoutActivity", "Không thể cập nhật số lượng cho sản phẩm: " + product.getName() + " - ID/Key là null");
                            continue; // Bỏ qua sản phẩm này và tiếp tục với sản phẩm tiếp theo
                        }

                        Log.d("CheckoutActivity", "Đang xử lý sản phẩm: " + product.getName());
                        Log.d("CheckoutActivity", "Category: " + productCategory + ", Key: " + productKey + ", Quantity mua: " + quantityPurchased);

                        // Tiếp tục với code cập nhật Firebase...
                        DatabaseReference quantityRef = FirebaseDatabase
                                                                .getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                                                                .getReference("products")
                                                                .child(productCategory)
                                                                .child(productKey)
                                                                .child("quantity");

                        quantityRef.get().addOnSuccessListener(snapshot -> {
                            // Lấy giá trị thô từ Firebase thay vì ép kiểu trực tiếp
                            Object rawValue = snapshot.getValue();
                            int currentQuantity = 0;

                            if (rawValue != null) {
                                if (rawValue instanceof String) {
                                    try {
                                        // Nếu là chuỗi, chuyển đổi sang int
                                        currentQuantity = Integer.parseInt((String) rawValue);
                                        Log.d("CheckoutActivity", "Chuyển đổi String sang Int: " + rawValue + " -> " + currentQuantity);
                                    } catch (NumberFormatException e) {
                                        Log.e("CheckoutActivity", "Lỗi chuyển đổi chuỗi sang số: " + e.getMessage());
                                    }
                                } else if (rawValue instanceof Long) {
                                    // Nếu là Long, chuyển đổi sang int
                                    currentQuantity = ((Long) rawValue).intValue();
                                    Log.d("CheckoutActivity", "Chuyển đổi Long sang Int: " + rawValue + " -> " + currentQuantity);
                                } else if (rawValue instanceof Integer) {
                                    // Nếu đã là Integer rồi
                                    currentQuantity = (Integer) rawValue;
                                    Log.d("CheckoutActivity", "Giá trị đã là Integer: " + currentQuantity);
                                } else if (rawValue instanceof Double) {
                                    // Nếu là Double, chuyển đổi sang int
                                    currentQuantity = ((Double) rawValue).intValue();
                                    Log.d("CheckoutActivity", "Chuyển đổi Double sang Int: " + rawValue + " -> " + currentQuantity);
                                } else {
                                    Log.w("CheckoutActivity", "Kiểu dữ liệu không xác định: " + rawValue.getClass().getName());
                                }
                            } else {
                                Log.w("CheckoutActivity", "Giá trị quantity là null, đặt mặc định = 0");
                            }

                            int updatedQuantity = currentQuantity - quantityPurchased;
                            if (updatedQuantity < 0) updatedQuantity = 0;

                            Log.d("CheckoutActivity", "Số lượng hiện tại: " + currentQuantity + ", Cập nhật còn: " + updatedQuantity);

                            quantityRef.setValue(updatedQuantity)
                                    .addOnSuccessListener(v -> Log.d("CheckoutActivity", "Cập nhật số lượng thành công cho: " + product.getName()))

                                    .addOnFailureListener(e -> Log.e("CheckoutActivity", "Lỗi cập nhật số lượng: " + e.getMessage()));

                        }).addOnFailureListener(e -> {
                            Log.e("CheckoutActivity", "Lỗi lấy số lượng sản phẩm từ Firebase: " + e.getMessage());
                        });
                    }

                    CartManager.getInstance().getCartItems().clear();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CheckoutActivity", "Lỗi khi đặt hàng: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onLocationFailed(String reason) {
        Toast.makeText(this, "Lỗi định vị: " + reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationHelper.stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates();
            } else {
                Toast.makeText(this, "Cần cấp quyền truy cập vị trí để sử dụng tính năng này", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Phương thức tiện ích để chuẩn hóa tất cả các trường số lượng trong cơ sở dữ liệu Firebase
     * Chuyển đổi tất cả các giá trị quantity từ String sang Integer
     * Bạn có thể gọi phương thức này từ một activity quản trị hoặc một lần khi setup ứng dụng
     */
    private void normalizeQuantityFields() {
        DatabaseReference productsRef = FirebaseDatabase
                                                .getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
                                                .getReference("products");

        productsRef.get().addOnSuccessListener(snapshot -> {
            for (com.google.firebase.database.DataSnapshot categorySnapshot : snapshot.getChildren()) {
                String category = categorySnapshot.getKey();

                for (com.google.firebase.database.DataSnapshot productSnapshot : categorySnapshot.getChildren()) {
                    String productKey = productSnapshot.getKey();
                    Object rawQuantity = productSnapshot.child("quantity").getValue();

                    if (rawQuantity instanceof String) {
                        try {
                            int numericQuantity = Integer.parseInt((String) rawQuantity);
                            productsRef.child(category).child(productKey).child("quantity").setValue(numericQuantity);
                            Log.d("DataNormalization", "Đã chuẩn hóa quantity cho " + category + "/" + productKey);
                        } catch (NumberFormatException e) {
                            Log.e("DataNormalization", "Không thể chuyển đổi: " + rawQuantity);
                        }
                    }
                }
            }
            Toast.makeText(this, "Quá trình chuẩn hóa dữ liệu đã hoàn tất", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e("DataNormalization", "Lỗi khi chuẩn hóa dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi chuẩn hóa dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}