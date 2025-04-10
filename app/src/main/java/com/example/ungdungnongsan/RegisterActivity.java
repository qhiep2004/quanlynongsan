package com.example.ungdungnongsan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class RegisterActivity extends AppCompatActivity {

	private EditText edtName, edtEmail, edtPassword, edtPhone, edtAddress;
	private Button btnRegister;
	private TextView tvLoginPrompt;
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);


		edtName = findViewById(R.id.edtName);
		edtEmail = findViewById(R.id.edtEmail);
		edtPassword = findViewById(R.id.edtPassword);
		edtPhone = findViewById(R.id.edtPhone);
		edtAddress = findViewById(R.id.edtAddress);
		btnRegister = findViewById(R.id.btnRegister);
		tvLoginPrompt = findViewById(R.id.tvLoginPrompt);


		mAuth = FirebaseAuth.getInstance();


		btnRegister.setOnClickListener(v -> {
			String name = edtName.getText().toString().trim();
			String email = edtEmail.getText().toString().trim();
			String password = edtPassword.getText().toString().trim();
			String phone = edtPhone.getText().toString().trim();
			String address = edtAddress.getText().toString().trim();


			if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
				Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
				return;
			}


			mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					String uid = mAuth.getCurrentUser().getUid();


					Users newUser = new Users(email, password, name, phone, address);


					DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
							                        .getReference("users")
							                        .child(uid);
					ref.setValue(newUser).addOnCompleteListener(refTask -> {
						if (refTask.isSuccessful()) {
							Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

							startActivity(new Intent(this, LoginActivity.class));
							finish();
						} else {
							Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		});


		tvLoginPrompt.setOnClickListener(v -> {

			Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		});
	}
}
