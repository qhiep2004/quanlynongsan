package com.example.ungdungnongsan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import android.content.Intent;
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

	private List<Product> productList;
	private Context context;

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


		holder.tvProductDesc.setText("Sản phẩm nông sản sạch");




		holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
		holder.itemView.setOnClickListener(v -> {
			Intent intent = new Intent(context, ProductDetailActivity.class);
			intent.putExtra("product", product);
			context.startActivity(intent);

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
		TextView tvProductName, tvProductDesc, tvPrice, tvOriginalPrice;

		public ProductViewHolder(@NonNull View itemView) {
			super(itemView);
			imgProduct = itemView.findViewById(R.id.imgProduct);
			tvProductName = itemView.findViewById(R.id.tvProductName);
			tvProductDesc = itemView.findViewById(R.id.tvProductDesc);
			tvPrice = itemView.findViewById(R.id.tvPrice);
			tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
		}
	}
}