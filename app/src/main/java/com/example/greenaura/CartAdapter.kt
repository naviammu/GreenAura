package com.example.greenaura

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private val cartList: ArrayList<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit,    // Lambda for Remove button click
    private val onQuantityChange: (Double) -> Unit    // Lambda to update total price in the activity
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // ViewHolder class to hold and recycle views
    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val productName: TextView = itemView.findViewById(R.id.textViewProductName)
        val productPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val productQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val removeButton: Button = itemView.findViewById(R.id.buttonRemove)

        // Quantity control buttons
        val quantityIncreaseButton: Button = itemView.findViewById(R.id.quantityIncreaseButton)
        val quantityDecreaseButton: Button = itemView.findViewById(R.id.quantityDecreaseButton)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartList[position]

        // Set product details
        holder.productName.text = cartItem.productName
        holder.productPrice.text = "₹${cartItem.price}"
        holder.productQuantity.text = cartItem.quantity.toString()

        // Load product image using Glide
        Glide.with(holder.itemView.context)
            .load(cartItem.imageUrl)
            .into(holder.productImage)

        // Handle Remove button click
        holder.removeButton.setOnClickListener {
            onRemoveClick(cartItem)
            cartList.removeAt(position)
            notifyItemRemoved(position)
            updateTotalPrice()
        }

        // Handle Increase quantity button click
        holder.quantityIncreaseButton.setOnClickListener {
            cartItem.quantity++  // Increase quantity
            holder.productQuantity.text = cartItem.quantity.toString()
            updateTotalPrice()  // Recalculate total price after quantity change
        }

        // Handle Decrease quantity button click
        holder.quantityDecreaseButton.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--  // Decrease quantity
                holder.productQuantity.text = cartItem.quantity.toString()
                updateTotalPrice()  // Recalculate total price after quantity change
            }
        }
    }

    // Function to calculate the total price and notify the activity
    private fun updateTotalPrice() {
        var totalPrice = 0.0
        for (item in cartList) {
            totalPrice += item.price * item.quantity
        }
        onQuantityChange(totalPrice)  // Notify the CartActivity about the updated total price
    }

    // Return the size of the cart list
    override fun getItemCount(): Int {
        return cartList.size
    }

}
