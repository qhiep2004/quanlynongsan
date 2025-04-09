package com.example.ungdungnongsan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

	private List<String> categories;
	private OnCategoryClickListener listener;

	public interface OnCategoryClickListener {
		void onCategoryClick(String categoryName);
	}

	public CategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
		this.categories = categories;
		this.listener = listener;
	}

	@NonNull
	@Override
	public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
		return new CategoryViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
		String category = categories.get(position);
		holder.textView.setText(category);
		holder.textView.setOnClickListener(v -> listener.onCategoryClick(category));
	}

	@Override
	public int getItemCount() {
		return categories.size();
	}

	public static class CategoryViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		public CategoryViewHolder(@NonNull View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.tvCategory);
		}
	}
}
