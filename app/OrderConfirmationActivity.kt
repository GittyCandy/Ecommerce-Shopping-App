package com.example.myecommerceapp2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        // Initialize views
        val tvConfirmationMessage: TextView = findViewById(R.id.tvConfirmationMessage)
        val btnContinueShopping: Button = findViewById(R.id.btnContinueShopping)

        // Set confirmation message
        tvConfirmationMessage.text = "Order Confirmed!"

        // Continue Shopping Button
        btnContinueShopping.setOnClickListener {
            val intent = Intent(this, EcommerceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
