package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GroupedSellerProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_PRODUCT = 1;

	private List<Object> itemList;
	private Context context;

	public GroupedSellerProductAdapter(Context context, List<Object> itemList) {
		this.context = context;
		this.itemList = itemList;
	}

	@Override
	public int getItemViewType(int position) {
		return (itemList.get(position) instanceof Product) ? TYPE_PRODUCT : TYPE_HEADER;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER) {
			View view = LayoutInflater.from(context).inflate(R.layout.item_category_header, parent, false);
			return new HeaderViewHolder(view);
		} else {
			View view = LayoutInflater.from(context).inflate(R.layout.item_product_seller, parent, false);
			return new ProductViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof HeaderViewHolder) {
			String category = (String) itemList.get(position);
			((HeaderViewHolder) holder).tvCategoryHeader.setText(category);
		} else {
			Product product = (Product) itemList.get(position);
			ProductViewHolder h = (ProductViewHolder) holder;

			h.tvName.setText(product.getName());
			h.tvPrice.setText(product.getPrice() + "đ");

			Glide.with(context)
					.load(product.getImageUrl())
					.placeholder(R.drawable.ic_launcher_background)
					.into(h.imgProduct);

			h.btnEdit.setOnClickListener(v -> {
				Intent intent = new Intent(context, AddEditProductActivity.class);
				intent.putExtra("product", product);
				context.startActivity(intent);
			});

			h.btnDelete.setOnClickListener(v -> {
				DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
						                        .getReference("products")
						                        .child(product.getCategory())
						                        .child(product.getKey());

				ref.removeValue().addOnSuccessListener(unused -> {
					Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
					itemList.remove(position);
					notifyItemRemoved(position);
					notifyItemRangeChanged(position, itemList.size());
				}).addOnFailureListener(e -> {
					Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				});
			});
		}
	}

	@Override
	public int getItemCount() {
		return itemList.size();
	}

	public static class HeaderViewHolder extends RecyclerView.ViewHolder {
		TextView tvCategoryHeader;

		public HeaderViewHolder(@NonNull View itemView) {
			super(itemView);
			tvCategoryHeader = itemView.findViewById(R.id.tvCategoryHeader);
		}
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
