package com.example.myecommerceapp2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the edit profile layout directly in the fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://myecommerceapp2-default-rtdb.firebaseio.com/")
            .getReference("MyUsers")

        // UI Elements
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etCity = view.findViewById<EditText>(R.id.etCity)
        val etCountry = view.findViewById<EditText>(R.id.etCountry)
        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etCardNumber = view.findViewById<EditText>(R.id.etCardNumber)
        val etExpiryDate = view.findViewById<EditText>(R.id.etExpiryDate)
        val etCVV = view.findViewById<EditText>(R.id.etCVV)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userEmail = currentUser.email

            tvUserEmail.text = userEmail

            // Fetch user data from Firebase
            database.child(userId).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    etName.setText(dataSnapshot.child("name").value.toString())
                    etCity.setText(dataSnapshot.child("city").value.toString())
                    etCountry.setText(dataSnapshot.child("country").value.toString())
                    etCardNumber.setText(dataSnapshot.child("cardNumber").value.toString())
                    etExpiryDate.setText(dataSnapshot.child("expiryDate").value.toString())
                    etCVV.setText(dataSnapshot.child("cvv").value.toString())
                }
            }.addOnFailureListener {
                Log.e("FirebaseError", "Failed to fetch user data")
            }

            btnUpdate.setOnClickListener {
                val newName = etName.text.toString().trim()
                val newCity = etCity.text.toString().trim()
                val newCountry = etCountry.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val newCardNumber = etCardNumber.text.toString().trim()
                val newExpiryDate = etExpiryDate.text.toString().trim()
                val newCVV = etCVV.text.toString().trim()

                if (newName.isNotEmpty() && newCity.isNotEmpty() && newCountry.isNotEmpty()) {
                    val updatedData = mapOf(
                        "name" to newName,
                        "city" to newCity,
                        "country" to newCountry,
                        "cardNumber" to newCardNumber,
                        "expiryDate" to newExpiryDate,
                        "cvv" to newCVV
                    )
                    database.child(userId).updateChildren(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Please fill in required fields.", Toast.LENGTH_SHORT).show()
                }

                if (newPassword.isNotEmpty() && newPassword.length >= 6) {
                    currentUser.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Password updated!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Failed to update password.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        return view
    }
}

data class DatabaseModel(
    val name: String? = null,
    val phone: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val email: String? = null,
    val password: String? = null,
    val cardNumber: String? = null,
    val expiryDate: String? = null,
    val cvv: String? = null
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", "")
}
