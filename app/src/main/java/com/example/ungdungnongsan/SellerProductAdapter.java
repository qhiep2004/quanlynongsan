package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;
import android.util.Log;
import com.bumptech.glide.Glide;

import java.util.List;
import android.widget.Button;
public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.ProductViewHolder> {
	private Context context;
	private List<Product> productList;
	private boolean isSellerView;



	public SellerProductAdapter(Context context, List<Product> productList) {
		this.context = context;
		this.productList = productList;

		this.isSellerView = context instanceof SellerProductsActivity;
	}

	@NonNull
	@Override
	public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_product1, parent, false);
		return new ProductViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
		Product product = productList.get(position);

		holder.tvProductName.setText(product.getName());
		holder.tvPrice.setText(product.getPrice() + "đ");
		holder.tvProductDesc.setText("Sản phẩm nông sản tươi");

		if (product.getPrice() != null && !product.getPrice().isEmpty()) {
			holder.tvOriginalPrice.setText(product.getPrice() + "đ");
			holder.tvOriginalPrice.setVisibility(View.VISIBLE);
		} else {
			holder.tvOriginalPrice.setVisibility(View.GONE);
		}

		Glide.with(context)
				.load(product.getImageUrl())
				.into(holder.imgProduct);

		holder.cardView.setOnClickListener(v -> {
			if (isSellerView) {

				Intent intent = new Intent(context, ProductDetailActivity.class);
				intent.putExtra("product", product);
				context.startActivity(intent);

			} else {

				Intent intent = new Intent(context, ProductDetailActivity.class);
				intent.putExtra("product", product);
				context.startActivity(intent);
			}
		});

		holder.btnEdit.setOnClickListener(v -> {
			Intent intent = new Intent(context, AddEditProductActivity.class);
			intent.putExtra("product", product);
			context.startActivity(intent);
		});

		holder.btnDelete.setOnClickListener(v -> {
			new androidx.appcompat.app.AlertDialog.Builder(context)
					.setTitle("Xóa sản phẩm")
					.setMessage("Bạn có chắc muốn xóa sản phẩm này?")
					.setPositiveButton("Xóa", (dialog, which) -> {
						deleteProduct(product);
					})
					.setNegativeButton("Hủy", null)
					.show();
		});

	}
	private void deleteProduct(Product product) {
		Log.d("DeleteProduct", "Category của sản phẩm: " + product.getCategory());
		Log.d("DeleteProduct", "ID của sản phẩm: " + product.getId());
		DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlynongsan-d0391-default-rtdb.asia-southeast1.firebasedatabase.app")
				                        .getReference("products").child(product.getCategory()).child(product.getId());

		ref.removeValue().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				productList.remove(product);
				notifyDataSetChanged();
				Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
			} else {
				Log.e("DeleteProduct", "Lỗi xóa sản phẩm: ", task.getException());
				Toast.makeText(context, "Lỗi xóa sản phẩm: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public int getItemCount() {
		return productList.size();
	}


	public void setSellerView(boolean isSellerView) {
		this.isSellerView = isSellerView;
	}

	static class ProductViewHolder extends RecyclerView.ViewHolder {
		ImageView imgProduct;
		TextView tvProductName, tvProductDesc, tvPrice, tvOriginalPrice;
		CardView cardView;
		Button btnEdit, btnDelete;

		public ProductViewHolder(@NonNull View itemView) {
			super(itemView);
			imgProduct = itemView.findViewById(R.id.imgProduct);
			tvProductName = itemView.findViewById(R.id.tvProductName);
			tvProductDesc = itemView.findViewById(R.id.tvProductDesc);
			tvPrice = itemView.findViewById(R.id.tvPrice);
			tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
			cardView = (CardView) itemView;
			btnEdit = itemView.findViewById(R.id.btnEdit);
			btnDelete = itemView.findViewById(R.id.btnDelete);


		}
	}
}