package com.example.greenaura

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for the RecyclerView that displays the list of addresses
class AddressAdapter(
    private var addressList: MutableList<Address>,  // Mutable list for dynamic updates
    private val onAddressEdit: ((Address) -> Unit),  // Callback for edit action
    private val onAddressDelete: ((Address) -> Unit) // Callback for delete action
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    // Inflate the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return AddressViewHolder(view)
    }

    // Bind the data to the ViewHolder (sets the address details to the TextView)
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]

        holder.addressTextView.text = formatAddressDetails(address)

        // Set custom colors for Edit button
        holder.editButton.setTextColor(holder.itemView.context.getColor(android.R.color.white))
        holder.editButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
            holder.itemView.context.getColor(R.color.green) // Make sure R.color.green exists
        )

        // Set custom colors for Delete button
        holder.deleteButton.setTextColor(holder.itemView.context.getColor(android.R.color.white))
        holder.deleteButton.backgroundTintList = android.content.res.ColorStateList.valueOf(
            holder.itemView.context.getColor(R.color.green) // Make sure R.color.red exists
        )

        // Set click listener for Edit button
        holder.editButton.setOnClickListener {
            onAddressEdit.invoke(address)  // Trigger the edit callback with the clicked address
        }

        // Set click listener for Delete button
        holder.deleteButton.setOnClickListener {
            onAddressDelete.invoke(address)  // Trigger the delete callback with the clicked address
        }
    }

    // Return the total number of items in the data set
    override fun getItemCount(): Int = addressList.size

    // Method to update the data set and notify the RecyclerView of changes
    fun updateAddressList(newAddressList: List<Address>) {
        addressList.clear()  // Clear the old list
        addressList.addAll(newAddressList)  // Add the new data
        notifyDataSetChanged()  // Notify RecyclerView that the data set has changed
    }

    // Helper method to format the address details into a readable string (excluding name)
    private fun formatAddressDetails(address: Address): String {
        return """
            Phone: ${address.phoneNumber}
            House: ${address.houseNumber}, ${address.StreetName}
            City: ${address.city}, ${address.state} - ${address.postalCode}
        """.trimIndent()
    }

    // ViewHolder class that holds the views for each item in the RecyclerView
    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addressTextView: TextView = itemView.findViewById(R.id.textViewAddress)  // Address text
        val editButton: Button = itemView.findViewById(R.id.buttonEditAddress)       // Edit button
        val deleteButton: Button = itemView.findViewById(R.id.buttonDeleteAddress)   // Delete button
    }
}
