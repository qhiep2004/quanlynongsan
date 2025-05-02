package com.example.ungdungnongsan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

	private List<String> categoryList;
	private OnCategoryClickListener listener;
	private int selectedPosition = 0; // Default to first item (All)
	private Map<String, Integer> categoryCounts;

	public interface OnCategoryClickListener {
		void onCategoryClick(String categoryName);
	}

	public CategoryAdapter(List<String> categoryList, OnCategoryClickListener listener) {
		this.categoryList = categoryList;
		this.listener = listener;
		this.categoryCounts = new HashMap<>();
	}

	@NonNull
	@Override
	public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
		return new CategoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
		String categoryName = categoryList.get(position);
		holder.tvCategory.setText(categoryName);

		// Update selection state
		holder.cardView.setCardBackgroundColor(position == selectedPosition ?
				holder.itemView.getContext().getResources().getColor(R.color.primary) :
				holder.itemView.getContext().getResources().getColor(R.color.accent));

		// Set category icon
		holder.ivCategoryIcon.setImageResource(getCategoryIcon(categoryName));

		// Set item count if available
		if (categoryCounts != null && categoryCounts.containsKey(categoryName)) {
			holder.tvItemCount.setText(String.valueOf(categoryCounts.get(categoryName)));
			holder.tvItemCount.setVisibility(View.VISIBLE);
		} else {
			holder.tvItemCount.setVisibility(View.GONE);
		}

		// Set click listener
		holder.cardView.setOnClickListener(v -> {
			int previousSelected = selectedPosition;
			selectedPosition = holder.getAdapterPosition();

			// Update UI for previous and newly selected items
			notifyItemChanged(previousSelected);
			notifyItemChanged(selectedPosition);

			// Notify listener
			if (listener != null) {
				listener.onCategoryClick(categoryName);
			}
		});
	}

	@Override
	public int getItemCount() {
		return categoryList.size();
	}

	public int getSelectedPosition() {
		return selectedPosition;
	}

	// Helper method to get the appropriate icon based on category name
	private int getCategoryIcon(String category) {
		switch (category.toLowerCase()) {
			case "cá":
				return R.drawable.ic_fish;
			case "thịt":
				return R.drawable.ic_meat;
			case "rau":
				return R.drawable.ic_vegetables;
			case "trái cây":
				return R.drawable.ic_fruits;
			case "tất cả":
			default:
				return R.drawable.ic_all_categories;
		}
	}

	// For displaying product counts per category
	public void setCategoryCounts(List<GroupProduct> groupList) {
		categoryCounts = new HashMap<>();

		// Add "All" category count (total of all products)
		int totalCount = 0;
		for (GroupProduct group : groupList) {
			String categoryName = group.getGroupName();
			int count = group.getProducts().size();
			totalCount += count;
			categoryCounts.put(categoryName, count);
		}
		categoryCounts.put("Tất cả", totalCount);

		notifyDataSetChanged();
	}

	static class CategoryViewHolder extends RecyclerView.ViewHolder {
		CardView cardView;
		TextView tvCategory;
		TextView tvItemCount;
		ImageView ivCategoryIcon;

		public CategoryViewHolder(@NonNull View itemView) {
			super(itemView);
			cardView = (CardView) itemView;
			tvCategory = itemView.findViewById(R.id.tvCategory);
			tvItemCount = itemView.findViewById(R.id.tvItemCount);
			ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
		}
	}
}