package com.example.ungdungnongsan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity1 extends AppCompatActivity {

	private EditText etName, etEmail, etPhone, etAddress;
	private Button btnSave, btnCancel;
	private ProgressBar progressBar;
	private FirebaseUser currentUser;
	private DatabaseReference userRef;
	private static final String TAG = "EditProfileActivity";
	private static final String FIREBASE_URL = "https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile1);


		etName = findViewById(R.id.etName);
		etEmail = findViewById(R.id.etEmail);
		etPhone = findViewById(R.id.etPhone);
		etAddress = findViewById(R.id.etAddress);
		btnSave = findViewById(R.id.btnSave);
		btnCancel = findViewById(R.id.btnCancel);
		progressBar = findViewById(R.id.progressBar);


		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}


		if (currentUser.getEmail() != null) {
			etEmail.setText(currentUser.getEmail());
		}
		etEmail.setEnabled(false);


		userRef = FirebaseDatabase.getInstance(FIREBASE_URL)
				          .getReference("users").child(currentUser.getUid());


		loadUserInfo();


		btnSave.setOnClickListener(v -> {
			if (isNetworkConnected()) {
				saveUserInfo();
			} else {
				Toast.makeText(this, "Không có kết nối internet", Toast.LENGTH_SHORT).show();
			}
		});


		btnCancel.setOnClickListener(v -> finish());
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
	}

	private void loadUserInfo() {
		progressBar.setVisibility(View.VISIBLE);

		userRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				progressBar.setVisibility(View.GONE);

				if (snapshot.exists()) {
					try {

						String name = snapshot.child("name").getValue(String.class);
						String phone = snapshot.child("phone").getValue(String.class);
						String address = snapshot.child("address").getValue(String.class);
						String email = snapshot.child("email").getValue(String.class);


						if (name != null) etName.setText(name);
						if (phone != null) etPhone.setText(phone);
						if (address != null) etAddress.setText(address);
						if (email != null) etEmail.setText(email);

						Log.d(TAG, "Loaded user data: name=" + name + ", phone=" + phone +
								           ", address=" + address + ", email=" + email);
					} catch (Exception e) {
						Log.e(TAG, "Error parsing user data", e);
						Toast.makeText(EditProfileActivity1.this, "Lỗi xử lý dữ liệu: " + e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Log.d(TAG, "User data not found");
					Toast.makeText(EditProfileActivity1.this, "Không tìm thấy dữ liệu người dùng",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				progressBar.setVisibility(View.GONE);
				Log.e(TAG, "Database error: " + error.getMessage());
				Toast.makeText(EditProfileActivity1.this, "Lỗi tải dữ liệu: " + error.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void saveUserInfo() {
		String name = etName.getText().toString().trim();
		String email = etEmail.getText().toString().trim();
		String phone = etPhone.getText().toString().trim();
		String address = etAddress.getText().toString().trim();


		if (name.isEmpty()) {
			etName.setError("Vui lòng nhập tên");
			etName.requestFocus();
			return;
		}

		if (email.isEmpty()) {
			etEmail.setError("Vui lòng nhập email");
			etEmail.requestFocus();
			return;
		}

		if (phone.isEmpty()) {
			etPhone.setError("Vui lòng nhập số điện thoại");
			etPhone.requestFocus();
			return;
		}

		if (address.isEmpty()) {
			etAddress.setError("Vui lòng nhập địa chỉ");
			etAddress.requestFocus();
			return;
		}


		progressBar.setVisibility(View.VISIBLE);


		Map<String, Object> updates = new HashMap<>();
		updates.put("name", name);
		updates.put("email", email);
		updates.put("phone", phone);
		updates.put("address", address);

		Log.d(TAG, "Updating user info: " + updates.toString());


		userRef.setValue(updates)
				.addOnSuccessListener(aVoid -> {
					progressBar.setVisibility(View.GONE);
					Log.d(TAG, "User info updated successfully");
					Toast.makeText(EditProfileActivity1.this, "Thông tin đã được cập nhật thành công!",
							Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					finish();
				})
				.addOnFailureListener(e -> {
					progressBar.setVisibility(View.GONE);
					Log.e(TAG, "Error updating user info: " + e.getMessage(), e);
					Toast.makeText(EditProfileActivity1.this, "Lỗi cập nhật: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				});
	}
}