package com.example.myecommerceapp2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference

        // Initialize views
        val btnGoBack: Button = findViewById(R.id.btnGoBack)
        val detailProductImage: ImageView = findViewById(R.id.detailProductImage)
        val detailProductName: TextView = findViewById(R.id.detailProductName)
        val detailProductPrice: TextView = findViewById(R.id.detailProductPrice)
        val detailProductDescription: TextView = findViewById(R.id.detailProductDescription)
        val detailProductRating: TextView = findViewById(R.id.detailProductRating)
        val detailProductCategory: TextView = findViewById(R.id.detailProductCategory)
        val detailSpecifications: TextView = findViewById(R.id.detailSpecifications)
        val btnAddToCart: Button = findViewById(R.id.btnAddToCart)


        // Get the product data from the intent
        val product = intent.getParcelableExtra<ProductData>("product")

        // Set the product details in the views
        if (product != null) {
            // Load image using Picasso
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(R.drawable.noimage) // Optional placeholder

                .error(R.drawable.noimage) // Optional error image
                .into(detailProductImage)

            detailProductName.text = product.name
            detailProductPrice.text = "$${product.price}"
            detailProductDescription.text = product.description
            detailProductRating.text = getStarEmojis(product.rating)
            detailProductCategory.text = "Category: ${product.category}"

            // Display specifications
            val specificationsText = StringBuilder("Specifications:\n")
            for ((key, value) in product.specifications) {
                specificationsText.append("• $key: $value\n")
            }
            detailSpecifications.text = specificationsText.toString()

            // Fetch and display feedback for this product

        }

        // Go Back Button
        btnGoBack.setOnClickListener {
            finish() // Close the activity and go back
        }

        // Add to Cart Button
        btnAddToCart.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null && product != null) {
                saveProductToCart(userId, product)
                Toast.makeText(this, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please log in to add to cart", Toast.LENGTH_SHORT).show()
            }
        }

        // Submit Feedback Button

    }

    // Helper function to generate star emojis
    private fun getStarEmojis(rating: Float): String {
        val fullStar = "⭐"
        val emptyStar = "☆"
        val maxStars = 5

        val fullStarsCount = rating.toInt()
        val emptyStarsCount = maxStars - fullStarsCount

        return fullStar.repeat(fullStarsCount) + emptyStar.repeat(emptyStarsCount)
    }

    // Save product to the user's cart in Firebase
    private fun saveProductToCart(userId: String, product: ProductData) {
        val cartRef = dbReference.child("users").child(userId).child("cart").child(product.name)
        cartRef.setValue(product)
    }

    // Save feedback to the product in Firebase
    private fun saveFeedback(productName: String, userId: String, feedback: String) {
        val feedbackRef = dbReference.child("products").child(productName).child("feedback").child(userId)
        feedbackRef.setValue(feedback)
    }

    // Fetch and display feedback for the product
    private fun fetchFeedback(productName: String, feedbackView: TextView) {
        val feedbackRef = dbReference.child("products").child(productName).child("feedback")
        feedbackRef.get().addOnSuccessListener { snapshot ->
            val feedbackList = StringBuilder("Feedback from users:\n")
            for (feedbackSnapshot in snapshot.children) {
                val userId = feedbackSnapshot.key
                val feedback = feedbackSnapshot.value.toString()

                // Fetch the user's email using their UID
                if (userId != null) {
                    auth.fetchSignInMethodsForEmail(userId).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val email = task.result?.signInMethods?.firstOrNull()
                            if (email != null) {
                                feedbackList.append("$email said: $feedback\n")
                                feedbackView.text = feedbackList.toString()
                            }
                        } else {
                            feedbackList.append("User $userId said: $feedback\n")
                            feedbackView.text = feedbackList.toString()
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch feedback", Toast.LENGTH_SHORT).show()
        }
    }
}