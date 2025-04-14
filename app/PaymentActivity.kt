package com.example.myecommerceapp2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PaymentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference
    private val CHANNEL_ID = "order_notification_channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference

        // Initialize views
        val rgPaymentOptions: RadioGroup = findViewById(R.id.rgPaymentOptions)
        val btnConfirmOrder: Button = findViewById(R.id.btnConfirmOrder)

        // Create Notification Channel
        createNotificationChannel()

        // Confirm Order Button
        btnConfirmOrder.setOnClickListener {
            val selectedId = rgPaymentOptions.checkedRadioButtonId

            when (selectedId) {
                R.id.rbSavedCard -> {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        dbReference.child("users").child(userId).get().addOnSuccessListener { dataSnapshot ->
                            if (dataSnapshot.exists()) {
                                val cardNumber = dataSnapshot.child("cardNumber").value.toString()
                                val expiryDate = dataSnapshot.child("expiryDate").value.toString()
                                val cvv = dataSnapshot.child("cvv").value.toString()

                                if (cardNumber.isNotEmpty() && expiryDate.isNotEmpty() && cvv.isNotEmpty()) {
                                    completePurchase()
                                    Toast.makeText(this, "Order Confirmed with Saved Card!", Toast.LENGTH_SHORT).show()
                                    sendOrderNotification()
                                    val intent = Intent(this, OrderConfirmationActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "No saved card details found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                R.id.rbCashOnDelivery -> {
                    completePurchase()
                    Toast.makeText(this, "Order Confirmed with Cash on Delivery!", Toast.LENGTH_SHORT).show()
                    sendOrderNotification()
                    val intent = Intent(this, OrderConfirmationActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    Toast.makeText(this, "Please select a payment option", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Create a notification channel (For Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Order Notifications"
            val descriptionText = "Notifications for confirmed orders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Send order confirmation notification
    private fun sendOrderNotification() {
        val intent = Intent(this, OrderConfirmationActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Order Confirmed")
            .setContentText("Your order has been successfully placed!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun completePurchase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            dbReference.child("users").child(userId).child("cart").get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val productName = child.child("name").getValue(String::class.java) ?: "Unnamed"
                        val priceDouble = child.child("price").getValue(Double::class.java)
                        val price = priceDouble?.toString() ?: child.child("price").getValue(String::class.java) ?: "0.0"
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: "https://via.placeholder.com/150"
                        savePurchase(userId, productName, price, imageUrl)
                    }
                }
                clearCart()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to complete purchase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePurchase(userId: String, productName: String, price: String, imageUrl: String) {
        val purchaseRef = FirebaseDatabase.getInstance("https://myecommerceapp2-default-rtdb.firebaseio.com/")
            .getReference("MyUsers").child(userId).child("purchaseHistory")
        val purchaseId = purchaseRef.push().key
        val purchaseData = mapOf(
            "productName" to productName,
            "price" to price,
            "imageUrl" to imageUrl
        )
        purchaseId?.let {
            purchaseRef.child(it).setValue(purchaseData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Purchase saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save purchase.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun clearCart() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            dbReference.child("users").child(userId).child("cart").removeValue()
                .addOnSuccessListener {
                    // Cart cleared successfully
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to clear cart", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
