package com.example.ungdungnongsan;

import java.util.ArrayList;
import android.util.Log;
public class CartManager {
    private static CartManager instance;
    private ArrayList<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public boolean addToCart(Product product) {
        // Kiểm tra số lượng sản phẩm trước khi thêm vào giỏ hàng
        int productQuantity = 0;
        try {
            Object quantityObj = product.getQuantity();
            if (quantityObj instanceof String) {
                productQuantity = Integer.parseInt((String) quantityObj);
            } else if (quantityObj instanceof Long) {
                productQuantity = ((Long) quantityObj).intValue();
            } else if (quantityObj instanceof Integer) {
                productQuantity = (Integer) quantityObj;
            } else if (quantityObj instanceof Double) {
                productQuantity = ((Double) quantityObj).intValue();
            }
        } catch (Exception e) {
            Log.e("CartManager", "Lỗi khi chuyển đổi số lượng: " + e.getMessage());
            return false;
        }

        // Không cho phép thêm sản phẩm hết hàng vào giỏ
        if (productQuantity <= 0) {
            return false;
        }

        // Đảm bảo không có giá trị ID nào bị null
        if (product.getId() == null && product.getKey() != null) {
            product.setId(product.getKey());
        } else if (product.getKey() == null && product.getId() != null) {
            product.setKey(product.getId());
        }

        // Nếu cả hai đều null, sử dụng tên sản phẩm làm key tạm thời
        if (product.getId() == null && product.getKey() == null) {
            String tempKey = "temp_" + product.getName().replaceAll("\\s+", "_").toLowerCase();
            product.setId(tempKey);
            product.setKey(tempKey);
        }

        // Kiểm tra xem sản phẩm có trong giỏ hàng chưa
        for (CartItem item : cartItems) {
            Product itemProduct = item.getProduct();

            // So sánh bằng ID hoặc Key (chọn ID làm chính)
            boolean isSameProduct = false;
            if (product.getId() != null && itemProduct.getId() != null) {
                isSameProduct = product.getId().equals(itemProduct.getId());
            } else if (product.getKey() != null && itemProduct.getKey() != null) {
                isSameProduct = product.getKey().equals(itemProduct.getKey());
            }

            if (isSameProduct) {
                // Kiểm tra nếu số lượng trong giỏ + 1 vượt quá số lượng có sẵn
                if (item.getQuantity() + 1 > productQuantity) {
                    // Không thể thêm nhiều hơn số lượng sản phẩm hiện có
                    return false;
                }
                item.setQuantity(item.getQuantity() + 1);
                return true;
            }
        }

        // Thêm mới vào giỏ hàng
        cartItems.add(new CartItem(product, 1));
        return true;
    }

    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
    }

    public boolean updateQuantity(CartItem item, int newQuantity) {
        // Lấy thông tin số lượng hiện có của sản phẩm
        Product product = item.getProduct();
        int availableQuantity = 0;

        try {
            Object quantityObj = product.getQuantity();
            if (quantityObj instanceof String) {
                availableQuantity = Integer.parseInt((String) quantityObj);
            } else if (quantityObj instanceof Long) {
                availableQuantity = ((Long) quantityObj).intValue();
            } else if (quantityObj instanceof Integer) {
                availableQuantity = (Integer) quantityObj;
            } else if (quantityObj instanceof Double) {
                availableQuantity = ((Double) quantityObj).intValue();
            }
        } catch (Exception e) {
            Log.e("CartManager", "Lỗi khi chuyển đổi số lượng: " + e.getMessage());
        }

        // Kiểm tra nếu số lượng mới vượt quá số lượng có sẵn
        if (newQuantity > availableQuantity) {
            return false;
        }

        if (newQuantity <= 0) {
            cartItems.remove(item);
        } else {
            item.setQuantity(newQuantity);
        }
        return true;
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    // Lấy tổng giá trị giỏ hàng
    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            try {
                // Chuyển đổi giá từ String (có thể có dấu phẩy, dấu chấm) sang double
                String priceStr = item.getProduct().getPrice().replaceAll("[.,]", "");
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (Exception e) {
                Log.e("CartManager", "Lỗi khi tính tổng tiền: " + e.getMessage());
            }
        }
        return total;
    }
}