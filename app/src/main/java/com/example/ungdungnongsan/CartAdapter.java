package com.example.ungdungnongsan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, CartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice() + "đ");
        holder.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

        Glide.with(context)
                .load(product.getImageUrl())
                .into(holder.ivProduct);

        holder.btnIncrease.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(cartItem, cartItem.getQuantity() + 1);
            notifyItemChanged(position);
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                CartManager.getInstance().updateQuantity(cartItem, cartItem.getQuantity() - 1);
                notifyItemChanged(position);
            } else {
                CartManager.getInstance().removeFromCart(cartItem);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
            }
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnDelete.setOnClickListener(v -> {
            CartManager.getInstance().removeFromCart(cartItem);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
            if (listener != null) listener.onItemRemoved();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnIncrease, btnDecrease, btnDelete;

        CartViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivCartProduct);
            tvName = itemView.findViewById(R.id.tvCartProductName);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}