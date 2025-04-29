package com.example.ungdungnongsan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class ProductStatsAdapter extends RecyclerView.Adapter<ProductStatsAdapter.ViewHolder> {

	private List<ProductStats> productStatsList;
	private DecimalFormat decimalFormat = new DecimalFormat("#,###");

	public ProductStatsAdapter(List<ProductStats> productStatsList) {
		this.productStatsList = productStatsList;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_stat, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		ProductStats stats = productStatsList.get(position);
		holder.tvProductName.setText(stats.getProductName());
		holder.tvQuantity.setText("Số lượng bán: " + stats.getTotalQuantity());
		holder.tvRevenue.setText("Doanh thu: " + decimalFormat.format(stats.getTotalRevenue()) + "đ");
	}

	@Override
	public int getItemCount() {
		return productStatsList.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView tvProductName, tvQuantity, tvRevenue;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			tvProductName = itemView.findViewById(R.id.tvProductName);
			tvQuantity = itemView.findViewById(R.id.tvQuantity);
			tvRevenue = itemView.findViewById(R.id.tvRevenue);
		}
	}
}
