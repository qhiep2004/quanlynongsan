package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {
	private List<Product> productList;
	private Context context;

	public AdminProductAdapter(List<Product> productList, Context context) {
		this.productList = productList;
		this.context = context;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Product product = productList.get(position);
		holder.tvName.setText(product.getName());
		holder.tvPrice.setText("Giá: " + product.getPrice());

		// Load ảnh từ URL bằng Glide
		Glide.with(context)
				.load(product.getImageUrl())
				.placeholder(R.drawable.ic_launcher_background)
				.error(R.drawable.ic_launcher_foreground)
				.into(holder.imgProduct);


		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(context, ProductDetailActivity.class);
			intent.putExtra("product", product);
			context.startActivity(intent);
		});

		holder.btnEdit.setOnClickListener(v -> {
			Intent intent = new Intent(context, AddEditProductActivity.class);
			intent.putExtra("product", product);
			context.startActivity(intent);
		});

		holder.btnDelete.setOnClickListener(v -> {
			DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
					                        .getReference("products")
					                        .child(product.getCategory())
					                        .child(product.getKey());

			ref.removeValue().addOnSuccessListener(unused -> {
				Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
				productList.remove(position);
				notifyItemRemoved(position);
				notifyItemRangeChanged(position, productList.size());
			}).addOnFailureListener(e -> {
				Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			});
		});
	}

	@Override
	public int getItemCount() {
		return productList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvName, tvPrice;
		ImageView imgProduct;
		Button btnEdit, btnDelete;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.tvName);
			tvPrice = itemView.findViewById(R.id.tvPrice);
			imgProduct = itemView.findViewById(R.id.imgProduct);
			btnEdit = itemView.findViewById(R.id.btnEdit);
			btnDelete = itemView.findViewById(R.id.btnDelete);
		}
	}
}
