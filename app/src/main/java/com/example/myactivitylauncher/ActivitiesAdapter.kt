package com.example.myactivitylauncher

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivitiesAdapter(
    private val pm: PackageManager,
    private var items: List<ActivityInfo>,
    private val onClick: (ActivityInfo) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.imageIcon)
        val nameView: TextView = view.findViewById(R.id.textAppName)
        val componentView: TextView = view.findViewById(R.id.textPackageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            // 先共用 item_package 的版型
            .inflate(R.layout.item_package, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val info = items[position]

        val label = info.loadLabel(pm)?.toString() ?: info.name
        val icon = info.loadIcon(pm)

        holder.iconView.setImageDrawable(icon)
        holder.nameView.text = label
        holder.componentView.text = info.name

        holder.itemView.setOnClickListener {
            onClick(info)
        }
    }

    override fun getItemCount(): Int = items.size


    fun submitList(newItems: List<ActivityInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
