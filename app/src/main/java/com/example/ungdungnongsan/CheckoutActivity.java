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
                    // Clear the cart after successful order
                    CartManager.getInstance().getCartItems().clear();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
}