package com.example.ungdungnongsan;

import android.content.Context;
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

		ProductAdapter productAdapter = new ProductAdapter(context, group.getProducts());
		holder.rvProducts.setLayoutManager(new GridLayoutManager(context, 2));
		holder.rvProducts.setAdapter(productAdapter);

		holder.tvViewAll.setOnClickListener(v -> {
			// Xử lý khi nhấn vào "Xem tất cả"
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