package com.example.ungdungnongsan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

	private EditText edtEmail, edtName, edtPhone, edtAddress;
	private Button btnSave;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		edtEmail = findViewById(R.id.edtEmail);
		edtName = findViewById(R.id.edtName);
		edtPhone = findViewById(R.id.edtPhone);
		edtAddress = findViewById(R.id.edtAddress);
		btnSave = findViewById(R.id.btnSave);

		SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
		edtEmail.setText(prefs.getString("email", ""));
		edtName.setText(prefs.getString("name", ""));
		edtPhone.setText(prefs.getString("phone", ""));
		edtAddress.setText(prefs.getString("address", ""));

		btnSave.setOnClickListener(v -> {
			String email = edtEmail.getText().toString().trim();
			String name = edtName.getText().toString().trim();
			String phone = edtPhone.getText().toString().trim();
			String address = edtAddress.getText().toString().trim();

			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("email", email);
			editor.putString("name", name);
			editor.putString("phone", phone);
			editor.putString("address", address);
			editor.apply();

			Toast.makeText(this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
			finish(); // Quay về màn trước
		});
	}
}
