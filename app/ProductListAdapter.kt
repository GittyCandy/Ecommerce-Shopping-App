package com.example.myecommerceapp2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso // Import Picasso

class ProductListAdapter(context: Context, private val products: List<ProductData>) : ArrayAdapter<ProductData>(context, 0, products) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val product = products[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_product_search, parent, false)

        val productImage = view.findViewById<ImageView>(R.id.productImage)
        val productName = view.findViewById<TextView>(R.id.productName)
        val productPrice = view.findViewById<TextView>(R.id.productPrice)
        val productDescription = view.findViewById<TextView>(R.id.productDescription)
        val productRating = view.findViewById<TextView>(R.id.productRating)
        val productCategory = view.findViewById<TextView>(R.id.productCategory)
        val productType = view.findViewById<TextView>(R.id.productType)

        // Load image using Picasso
        Picasso.get()
            .load(product.imageUrl) // Use the imageUrl field from ProductData
            .placeholder(R.drawable.noimage) // Optional placeholder
            .error(R.drawable.noimage) // Optional error image
            .into(productImage)

        productName.text = product.name
        productPrice.text = "$${product.price}"
        productDescription.text = product.description

        // Generate star emojis based on the rating
        val rating = product.rating
        val starEmojis = getStarEmojis(rating)
        productRating.text = starEmojis

        productCategory.text = "Category: ${product.category}"
        productType.text = "Type: ${product.type}"
        return view
    }

    // Helper function to generate star emojis based on the rating
    private fun getStarEmojis(rating: Float): String {
        val fullStar = "⭐"
        val halfStar = "½"
        val emptyStar = "☆"
        val maxStars = 5

        val fullStarsCount = rating.toInt()
        val hasHalfStar = (rating - fullStarsCount) >= 0.5
        val emptyStarsCount = maxStars - fullStarsCount - if (hasHalfStar) 1 else 0

        return fullStar.repeat(fullStarsCount) + if (hasHalfStar) halfStar else "" + emptyStar.repeat(emptyStarsCount)
    }
}