package com.example.myecommerceapp2

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myecommerceapp2.databinding.ItemProductBinding
import com.squareup.picasso.Picasso

class ProductAdapter(private val products: List<ProductData>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductData) {
            binding.productName.text = product.name
            binding.productPrice.text = "$${product.price}"

            // Load image using Picasso
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(R.drawable.noimage) // Optional placeholder
                .error(R.drawable.noimage) // Optional error image
                .into(binding.productImage)

            // Set click listener for the item
            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ProductDetailActivity::class.java).apply {
                    putExtra("product", product) // Pass the product data to the detail activity
                }
                binding.root.context.startActivity(intent)
            }
        }
    }
}