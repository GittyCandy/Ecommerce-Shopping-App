package com.example.myecommerceapp2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class CartFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var recentPurchasesRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var recentPurchasesAdapter: RecentPurchasesAdapter
    private lateinit var tvTotalPrice: TextView
    private lateinit var tvDeliveryStatus: TextView
    private lateinit var deliveryProgressLayout: LinearLayout
    private val cartItems = mutableListOf<CartItem>()
    private val recentPurchases = mutableListOf<ProductData>()
    private val handler = Handler(Looper.getMainLooper())
    private val deliveryStages = listOf("Confirmed", "Shipped", "Out for Delivery", "Completed")
    private var currentDeliveryStage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.reference

        // Initialize views
        val btnProceedToPayment: Button = view.findViewById(R.id.btnProceedToPayment)
        cartRecyclerView = view.findViewById(R.id.recyclerViewCart)
        recentPurchasesRecyclerView = view.findViewById(R.id.recyclerViewRecentPurchases)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        tvDeliveryStatus = view.findViewById(R.id.tvDeliveryStatus)
        deliveryProgressLayout = view.findViewById(R.id.deliveryProgressLayout)

        // Set up RecyclerViews
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recentPurchasesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = CartAdapter(cartItems, ::removeItem, ::updateQuantity)
        recentPurchasesAdapter = RecentPurchasesAdapter(recentPurchases)
        cartRecyclerView.adapter = cartAdapter
        recentPurchasesRecyclerView.adapter = recentPurchasesAdapter

        // Load cart items and purchase history
        loadCartItems()
        loadRecentPurchases()

        // Proceed to Payment Button
        btnProceedToPayment.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                // Navigate to Payment Activity
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Simulate delivery progress
        simulateDeliveryProgress()

        return view
    }

    // Load current cart items from "users/{userId}/cart"
    private fun loadCartItems() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            dbReference.child("users").child(userId).child("cart")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartItems.clear()
                        for (itemSnapshot in snapshot.children) {
                            val product = itemSnapshot.getValue(ProductData::class.java)
                            if (product != null) {
                                val cartItem = CartItem(product, 1) // Default quantity is 1
                                cartItems.add(cartItem)
                            }
                        }
                        cartAdapter.notifyDataSetChanged()
                        updateTotalPrice()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load cart items", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // Load purchase history from "MyUsers/{userId}/purchaseHistory" to display under "Keep Shopping"
    private fun loadRecentPurchases() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val purchaseRef = FirebaseDatabase.getInstance("https://myecommerceapp2-default-rtdb.firebaseio.com/")
                .getReference("MyUsers").child(userId).child("purchaseHistory")
            purchaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    recentPurchases.clear()
                    for (itemSnapshot in snapshot.children) {
                        val productName = itemSnapshot.child("productName").getValue(String::class.java)
                        val priceStr = itemSnapshot.child("price").getValue(String::class.java)
                        val price = priceStr?.toDoubleOrNull() ?: 0.0
                        // Use Picasso to load the image; here a placeholder image is used if no image URL is available
                        val product = ProductData(
                            name = productName ?: "Unnamed",
                            price = price,
                            imageUrl = "https://via.placeholder.com/150"
                        )
                        recentPurchases.add(product)
                    }
                    recentPurchasesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load Keep Shopping items", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun removeItem(position: Int) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val productName = cartItems[position].product.name
            dbReference.child("users").child(userId).child("cart").child(productName).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateQuantity(position: Int, newQuantity: Int) {
        if (newQuantity > 0) {
            cartItems[position].quantity = newQuantity
            cartAdapter.notifyItemChanged(position)
            updateTotalPrice()
        } else {
            removeItem(position)
        }
    }

    private fun updateTotalPrice() {
        var total = 0.0
        for (item in cartItems) {
            total += item.product.price * item.quantity
        }
        tvTotalPrice.text = "Total: $${String.format("%.2f", total)}"
    }

    private fun simulateDeliveryProgress() {
        val runnable = object : Runnable {
            override fun run() {
                if (currentDeliveryStage < deliveryStages.size) {
                    updateDeliveryProgress(currentDeliveryStage)
                    currentDeliveryStage++
                    handler.postDelayed(this, 3000) // Update every 10 seconds
                }
            }
        }
        handler.postDelayed(runnable, 10000) // Start after 10 seconds
    }

    private fun updateDeliveryProgress(stage: Int) {
        tvDeliveryStatus.text = "Delivery Status: ${deliveryStages[stage]}"

        val confirmedIcon = view?.findViewById<ImageView>(R.id.ivConfirmed)
        val shippedIcon = view?.findViewById<ImageView>(R.id.ivShipped)
        val outForDeliveryIcon = view?.findViewById<ImageView>(R.id.ivOutForDelivery)
        val completedIcon = view?.findViewById<ImageView>(R.id.ivCompleted)

        when (stage) {
            0 -> confirmedIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.pending))
            1 -> shippedIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.shipped))
            2 -> outForDeliveryIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.outForDelivery))
            3 -> completedIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.completed))
        }
    }
}

// Data class for Cart Item
data class CartItem(
    val product: ProductData,
    var quantity: Int
)

// Adapter for Cart Items
class CartAdapter(
    private val cartItems: List<CartItem>,
    private val removeItem: (Int) -> Unit,
    private val updateQuantity: (Int, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item, removeItem, updateQuantity)
    }

    override fun getItemCount(): Int = cartItems.size

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val productName: TextView = itemView.findViewById(R.id.tvProductName)
        private val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val productQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
        private val btnIncrease: Button = itemView.findViewById(R.id.btnIncrease)

        fun bind(item: CartItem, removeItem: (Int) -> Unit, updateQuantity: (Int, Int) -> Unit) {
            // Use Picasso to load the product image
            Picasso.get()
                .load(item.product.imageUrl)
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .into(productImage)

            productName.text = item.product.name
            productPrice.text = "$${item.product.price}"
            productQuantity.text = item.quantity.toString()

            btnRemove.setOnClickListener { removeItem(adapterPosition) }
            btnIncrease.setOnClickListener {
                val newQuantity = item.quantity + 1
                updateQuantity(adapterPosition, newQuantity)
            }
        }
    }
}

// Adapter for Recent Purchases (Keep Shopping)
class RecentPurchasesAdapter(
    private val recentPurchases: List<ProductData>
) : RecyclerView.Adapter<RecentPurchasesAdapter.RecentPurchaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentPurchaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_purchase, parent, false)
        return RecentPurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentPurchaseViewHolder, position: Int) {
        val product = recentPurchases[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = recentPurchases.size

    class RecentPurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val productName: TextView = itemView.findViewById(R.id.tvProductName)
        private val productPrice: TextView = itemView.findViewById(R.id.tvProductPrice)

        fun bind(product: ProductData) {
            // Use Picasso to load the product image for recent purchases
            Picasso.get()
                .load(product.imageUrl)
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .into(productImage)
            productName.text = product.name
            productPrice.text = "$${product.price}"
        }
    }
}
