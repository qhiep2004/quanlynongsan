package com.example.ungdungnongsan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

	private EditText edtEmail, edtPassword;
	private Button btnLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		edtEmail = findViewById(R.id.edtEmail);
		edtPassword = findViewById(R.id.edtPassword);
		btnLogin = findViewById(R.id.btnLogin);

		btnLogin.setOnClickListener(v -> {
			String email = edtEmail.getText().toString().trim();
			String password = edtPassword.getText().toString().trim();

			if (email.isEmpty() || password.isEmpty()) {
				Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
				return;
			}

			DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
					                             .getReference("users");

			usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					boolean isValid = false;

					for (DataSnapshot userSnapshot : snapshot.getChildren()) {
						String dbEmail = userSnapshot.child("email").getValue(String.class);
						String dbPassword = userSnapshot.child("password").getValue(String.class);

						if (email.equals(dbEmail) && password.equals(dbPassword)) {
							isValid = true;


							SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString("email", dbEmail);
							editor.apply();

							break;
						}
					}

					if (isValid) {
						Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finish();
					} else {
						Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError error) {
					Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		});
	}
}
