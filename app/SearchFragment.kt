package com.example.myecommerceapp2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.random.Random

class SearchFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val dbReference = database.getReference("products")
    private val productList = ArrayList<ProductData>()
    private lateinit var adapter: ProductListAdapter
    private lateinit var listView: ListView
    private lateinit var etSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        etSearch = rootView.findViewById(R.id.etSearch)
        listView = rootView.findViewById(R.id.listView)

        auth = FirebaseAuth.getInstance()

        adapter = ProductListAdapter(requireContext(), productList)
        listView.adapter = adapter

        // Load random products by default
        fetchRandomProducts()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    fetchRandomProducts() // Show random products when search is empty
                } else {
                    fetchProducts(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val product = productList[position]
            val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
                putExtra("product", product)
            }
            startActivity(intent)
        }

        return rootView
    }

    private fun fetchProducts(query: String) {
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductData::class.java)
                    product?.let {
                        if (it.name.contains(query, ignoreCase = true) ||
                            it.type.contains(query, ignoreCase = true)
                        ) {
                            productList.add(it)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRandomProducts() {
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<ProductData>()
                for (productSnapshot in snapshot.children) {
                    productSnapshot.getValue(ProductData::class.java)?.let { allProducts.add(it) }
                }

                // Shuffle and get up to 10 random products
                productList.clear()
                productList.addAll(allProducts.shuffled().take(10))
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
