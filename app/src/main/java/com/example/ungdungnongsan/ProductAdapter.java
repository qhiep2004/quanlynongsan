package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
	private Context context;
	private List<Product> productList;

	public ProductAdapter(Context context, List<Product> productList) {
		this.context = context;
		this.productList = productList;
	}

	@NonNull
	@Override
	public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
		return new ProductViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
		Product product = productList.get(position);
		holder.tvProductName.setText(product.getName());
		holder.tvPrice.setText(product.getPrice() + "đ");

		// Set origin if available
		if (product.getOrigin() != null && !product.getOrigin().isEmpty()) {
			holder.tvOrigin.setText(product.getOrigin());
			holder.tvOrigin.setVisibility(View.VISIBLE);
		} else {
			holder.tvOrigin.setVisibility(View.GONE);
		}

		// You can add harvest date if you have it in your product model
		// For now we'll hide it
		holder.tvHarvestDate.setVisibility(View.GONE);

		// Set up click listener for the entire item
		holder.itemView.setOnClickListener(v -> {
			try {
				Intent intent = new Intent(context, ProductDetailActivity.class);
				intent.putExtra("product", product);
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		Glide.with(context)
				.load(product.getImageUrl())
				.placeholder(R.drawable.ic_launcher_background)
				.error(R.drawable.ic_launcher_background)
				.into(holder.imgProduct);
	}

	@Override
	public int getItemCount() {
		return productList.size();
	}

	public static class ProductViewHolder extends RecyclerView.ViewHolder {
		ImageView imgProduct;
		TextView tvProductName, tvPrice, tvOrigin, tvHarvestDate;

		public ProductViewHolder(@NonNull View itemView) {
			super(itemView);
			imgProduct = itemView.findViewById(R.id.imgProduct);
			tvProductName = itemView.findViewById(R.id.tvProductName);
			tvPrice = itemView.findViewById(R.id.tvPrice);
			tvOrigin = itemView.findViewById(R.id.tvOrigin);
			tvHarvestDate = itemView.findViewById(R.id.tvHarvestDate);
		}
	}
}