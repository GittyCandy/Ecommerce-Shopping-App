package com.example.myecommerceapp2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myecommerceapp2.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private val handler = Handler(Looper.getMainLooper())
    private var currentItem = 0
    private val images = listOf(R.drawable.bigsale2, R.drawable.bigsale3)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()

        // Fetch products from Realtime Database
        fetchProducts()

        // Set up the banner slider
        val adapter = ViewPagerAdapter(images)
        binding.viewPager.adapter = adapter
        startAutoSwipe()

        return binding.root
    }

    private fun fetchProducts() {
        val productsRef = database.getReference("products")

        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<ProductData>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductData::class.java)
                    if (product != null) {
                        allProducts.add(product)
                    }
                }

                // Filter products for Featured and Best Sellers
                val featuredProducts = allProducts.shuffled().take(6)
// Example: First 6 products as featured
                val bestSellers = allProducts.filter { it.rating >= 4.5f } // Products with 4.5+ rating

                // Filter products by category
                val electronicsProducts = allProducts.filter { it.category.equals("Gaming Consoles", ignoreCase = true) }
                val fashionProducts = allProducts.filter { it.category.equals("Accessories", ignoreCase = true) }
                val homeAppliancesProducts = allProducts.filter { it.category.equals("Computers & Accessories", ignoreCase = true) }

                // Set up RecyclerViews
                setupRecyclerView(binding.featuredRecyclerView, featuredProducts, "Featured Products")
                setupRecyclerView(binding.bestSellersRecyclerView, bestSellers, "Best Sellers")
                setupRecyclerView(binding.electronicsRecyclerView, electronicsProducts, "Top Deals on Consoles")
                setupRecyclerView(binding.fashionRecyclerView, fashionProducts, "Trending in Accessories")
                setupRecyclerView(binding.homeAppliancesRecyclerView, homeAppliancesProducts, "Computers & Accessories")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(requireContext(), "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        products: List<ProductData>,
        sectionTitle: String
    ) {
        if (products.isNotEmpty()) {
            val adapter = ProductAdapter(products)
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
        } else {
            // Hide the section if no products are available
            recyclerView.visibility = View.GONE
            val titleView = when (recyclerView.id) {
                R.id.featuredRecyclerView -> binding.featuredTitle
                R.id.bestSellersRecyclerView -> binding.bestSellersTitle
                R.id.electronicsRecyclerView -> binding.electronicsTitle
                R.id.fashionRecyclerView -> binding.fashionTitle
                R.id.homeAppliancesRecyclerView -> binding.homeAppliancesTitle
                else -> null
            }
            titleView?.visibility = View.GONE
        }
    }

    private fun startAutoSwipe() {
        val runnable = object : Runnable {
            override fun run() {
                currentItem = (currentItem + 1) % images.size
                binding.viewPager.setCurrentItem(currentItem, true)
                handler.postDelayed(this, 2000)
            }
        }
        handler.postDelayed(runnable, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}