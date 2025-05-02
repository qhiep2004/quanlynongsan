package com.example.ungdungnongsan;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class ProductOriginActivity extends AppCompatActivity {

    private ImageView ivProductImage, ivCertificateImage;
    private TextView tvProductName, tvProductOrigin, tvHarvestDate;
    private TextView tvCertificateName, tvOrganization, tvIssueDate, tvExpiryDate;
    private TextView tvCompanyName, tvCompanyAddress, tvCompanyContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_origin);

        // Correct Toolbar import
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // Initialize views
        initViews();

        // Get product from intent
        Product product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            displayProductInfo(product);
            displayCertificateInfo(product.getCertificate());
            displayCompanyInfo(product);
        } else {
            Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        ivCertificateImage = findViewById(R.id.ivCertificateImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductOrigin = findViewById(R.id.tvProductOrigin);
        tvHarvestDate = findViewById(R.id.tvHarvestDate);
        tvCertificateName = findViewById(R.id.tvCertificateName);
        tvOrganization = findViewById(R.id.tvOrganization);
        tvIssueDate = findViewById(R.id.tvIssueDate);
        tvExpiryDate = findViewById(R.id.tvExpiryDate);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvCompanyContact = findViewById(R.id.tvCompanyContact);
    }

    private void displayProductInfo(Product product) {
        tvProductName.setText(product.getName());
        tvProductOrigin.setText(product.getOrigin());

        // Fix: Product doesn't have getHarvestDate() method
        // Use a placeholder or remove this line
        tvHarvestDate.setText("Thu hoạch: N/A");

        // Load product image using Glide
        Glide.with(this)
                .load(product.getImageUrl())
                .into(ivProductImage);
    }

    private void displayCertificateInfo(Certificate certificate) {
        if (certificate != null) {
            tvCertificateName.setText(certificate.getName());
            tvOrganization.setText(certificate.getOrganization());
            tvIssueDate.setText(certificate.getIssueDate());
            tvExpiryDate.setText(certificate.getExpiryDate());

            // Load certificate image
            if (certificate.getImageURL() != null && !certificate.getImageURL().isEmpty()) {
                Glide.with(this)
                        .load(certificate.getImageURL())
                        .placeholder(R.drawable.placeholder_certificate)
                        .error(R.drawable.error_image)
                        .into(ivCertificateImage);
            } else {
                ivCertificateImage.setImageResource(R.drawable.placeholder_certificate);
            }
        } else {
            // Handle case when no certificate is available
            tvCertificateName.setText("Không có chứng nhận");
            tvOrganization.setText("Không có thông tin");
            tvIssueDate.setText("N/A");
            tvExpiryDate.setText("N/A");
            ivCertificateImage.setVisibility(View.GONE);
        }
    }

    private void displayCompanyInfo(Product product) {
        // This would come from your product data or a separate company object
        // For now, using placeholder data
        tvCompanyName.setText("Công ty TNHH Nông sản Sạch XYZ");
        tvCompanyAddress.setText("123 Đường ABC, Quận 1, TP. Hồ Chí Minh");
        tvCompanyContact.setText("Email: info@nongsansach.com | SĐT: 0123456789");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}