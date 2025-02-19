package com.example.ritik_2

import android.content.Context
import android.widget.Toast
import com.example.ritik_2.ui.theme.Complaint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ComplaintRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getComplaints(onResult: (List<Complaint>) -> Unit, onError: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            onError()
            return
        }

        val uid = user.uid

        firestore.collection("users").document(uid).collection("complaints")
            .get()
            .addOnSuccessListener { result ->
                val complaints = result.documents.mapNotNull { doc ->
                    Complaint(
                        id = doc.id,
                        complainText = doc.getString("complainText") ?: "",
                        urgency = doc.getString("urgency") ?: "Normal",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        status = doc.getString("status") ?: "Open"
                    )
                }
                onResult(complaints)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load complaints", Toast.LENGTH_SHORT).show()
                onError()
            }
    }

    fun updateComplaintStatus(id: String, status: String) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users").document(uid).collection("complaints").document(id)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteComplaint(id: String, onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users").document(uid).collection("complaints").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Complaint deleted", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to delete complaint", Toast.LENGTH_SHORT).show()
            }
    }
}
