package com.example.myactivitylauncher

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PackagesAdapter(
    private val pm: PackageManager,
    private var items: List<ApplicationInfo>,
    private val onClick: (ApplicationInfo) -> Unit
) : RecyclerView.Adapter<PackagesAdapter.PackageViewHolder>() {

    inner class PackageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.imageIcon)
        val nameView: TextView = view.findViewById(R.id.textAppName)
        val packageView: TextView = view.findViewById(R.id.textPackageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val info = items[position]
        val label = pm.getApplicationLabel(info).toString()
        val icon = pm.getApplicationIcon(info)

        holder.iconView.setImageDrawable(icon)
        holder.nameView.text = label
        holder.packageView.text = info.packageName

        holder.itemView.setOnClickListener {
            onClick(info)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ApplicationInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
