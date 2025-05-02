package com.example.ungdungnongsan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupProductAdapter extends RecyclerView.Adapter<GroupProductAdapter.GroupViewHolder> {

	private Context context;
	private List<GroupProduct> groupList;

	public GroupProductAdapter(Context context, List<GroupProduct> groupList) {
		this.context = context;
		this.groupList = groupList;
	}
	public void setData(List<GroupProduct> newList) {
		this.groupList = newList;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
		return new GroupViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
		GroupProduct group = groupList.get(position);
		holder.tvGroupName.setText(group.getGroupName());

		// Initially show only a limited number of products (e.g., first 4)
		List<Product> displayedProducts = group.getProducts();
		if (displayedProducts.size() > 4) {
			displayedProducts = displayedProducts.subList(0, 4);
		}

		ProductAdapter productAdapter = new ProductAdapter(context, displayedProducts);
		holder.rvProducts.setLayoutManager(new GridLayoutManager(context, 2));
		holder.rvProducts.setAdapter(productAdapter);

		// Show "View All" button only if there are more products
		holder.tvViewAll.setVisibility(group.getProducts().size() > 4 ? View.VISIBLE : View.GONE);

		holder.tvViewAll.setOnClickListener(v -> {
			// Create intent to CategoryProductsActivity
			Intent intent = new Intent(context, CategoryProductsActivity.class);
			intent.putExtra("categoryName", group.getGroupName());
			context.startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		return groupList.size();
	}

	public static class GroupViewHolder extends RecyclerView.ViewHolder {
		TextView tvGroupName, tvViewAll;
		RecyclerView rvProducts;

		public GroupViewHolder(@NonNull View itemView) {
			super(itemView);
			tvGroupName = itemView.findViewById(R.id.tvGroupName);
			tvViewAll = itemView.findViewById(R.id.tvViewAll);
			rvProducts = itemView.findViewById(R.id.rvProducts);
		}

	}

}