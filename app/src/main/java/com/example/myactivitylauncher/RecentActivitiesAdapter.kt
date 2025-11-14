package com.example.myactivitylauncher

import android.content.ComponentName
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myactivitylauncher.data.RecentItem

class RecentActivitiesAdapter(
    private val pm: PackageManager,
    private var items: List<RecentItem>,
    private val onClick: (RecentItem) -> Unit,
    private val onLongClick: (RecentItem) -> Unit
) : RecyclerView.Adapter<RecentActivitiesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.imageIcon)
        val nameView: TextView = view.findViewById(R.id.textAppName)
        val packageView: TextView = view.findViewById(R.id.textPackageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]


        val component = ComponentName(item.packageName, item.activityName)
        val icon = try {
            pm.getActivityIcon(component)
        } catch (_: Exception) {
            null
        }
        icon?.let { holder.iconView.setImageDrawable(it) }


        holder.nameView.text = item.label
        holder.packageView.text = "${item.packageName}\n${item.activityName}"

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<RecentItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
