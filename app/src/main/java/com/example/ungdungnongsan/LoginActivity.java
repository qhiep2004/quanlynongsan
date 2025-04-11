package com.example.ungdungnongsan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

	private EditText edtEmail, edtPassword;
	private Button btnLogin;
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		edtEmail = findViewById(R.id.edtEmail);
		edtPassword = findViewById(R.id.edtPassword);
		btnLogin = findViewById(R.id.btnLogin);
		TextView tvSignUp = findViewById(R.id.tvSignUp);
		tvSignUp.setOnClickListener(v -> {
			startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
		});

		mAuth = FirebaseAuth.getInstance();

		btnLogin.setOnClickListener(v -> {
			String email = edtEmail.getText().toString().trim();
			String password = edtPassword.getText().toString().trim();

			if (email.isEmpty() || password.isEmpty()) {
				Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
				return;
			}

			mAuth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener(task -> {
						if (task.isSuccessful()) {
							FirebaseUser firebaseUser = mAuth.getCurrentUser();

							if (firebaseUser != null) {

								DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
										                             .getReference("users")
										                             .child(firebaseUser.getUid());

								usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
									@Override
									public void onDataChange(DataSnapshot snapshot) {
										Users user = snapshot.getValue(Users.class);
										if (user != null) {
											SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
											SharedPreferences.Editor editor = prefs.edit();
											editor.putString("email", user.getEmail());
											editor.putString("name", user.getName());
											editor.putString("phone", user.getPhone());
											editor.putString("address", user.getAddress());
											editor.apply();

											Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();


											String role = snapshot.child("role").getValue(String.class);
											if ("admin".equals(role)) {
												startActivity(new Intent(LoginActivity.this, AdminActivity.class));
											} else if ("seller".equals(role)) {
												startActivity(new Intent(LoginActivity.this, SellerProductsActivity.class));
											} else {
												startActivity(new Intent(LoginActivity.this, MainActivity.class));
											}

											finish();
										}
									}

									@Override
									public void onCancelled(DatabaseError error) {
										Toast.makeText(LoginActivity.this, "Lỗi đọc dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
									}
								});
							}
						} else {
							Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
						}
					});
		});
	}
}