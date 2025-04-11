package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.ProductViewHolder> {
	private Context context;
	private List<Product> productList;

	public SellerProductAdapter(Context context, List<Product> productList) {
		this.context = context;
		this.productList = productList;
	}

	@NonNull
	@Override
	public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_product_seller, parent, false);
		return new ProductViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
		Product product = productList.get(position);
		holder.tvName.setText(product.getName());
		holder.tvPrice.setText(product.getPrice() + "đ");

		Glide.with(context)
				.load(product.getImageUrl())
				.placeholder(R.drawable.ic_launcher_background)
				.into(holder.imgProduct);


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

	public static class ProductViewHolder extends RecyclerView.ViewHolder {
		ImageView imgProduct;
		TextView tvName, tvPrice;
		MaterialButton btnEdit, btnDelete;

		public ProductViewHolder(@NonNull View itemView) {
			super(itemView);
			imgProduct = itemView.findViewById(R.id.imgProduct);
			tvName = itemView.findViewById(R.id.tvName);
			tvPrice = itemView.findViewById(R.id.tvPrice);
			btnEdit = itemView.findViewById(R.id.btnEdit);
			btnDelete = itemView.findViewById(R.id.btnDelete);
		}
	}
}
