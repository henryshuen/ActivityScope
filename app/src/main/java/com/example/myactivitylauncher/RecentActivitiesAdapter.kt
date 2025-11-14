package com.example.myactivitylauncher

import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myactivitylauncher.data.RecentItem

class RecentActivitiesAdapter(
    private val pm: PackageManager,
    private var items: MutableList<RecentItem>, // This "items" will be the "currently displayed" list
    private val onItemClick: (RecentItem) -> Unit,
    private val onItemLongClick: (RecentItem) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<RecentActivitiesAdapter.ViewHolder>() {

    /** Whether multi-select mode is active */
    private var multiSelectMode = false

    /** Set of items currently selected */
    private val selectedItems = LinkedHashSet<RecentItem>()

    /** (NEW) Stores the original, unfiltered complete list */
    private var originalList: MutableList<RecentItem> = items.toMutableList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imageIcon)
        val name: TextView = view.findViewById(R.id.textAppName)
        val pkg: TextView = view.findViewById(R.id.textPackageName)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        init {
            /** Normal click */
            view.setOnClickListener {
                // (FIX) Ensure the click is on the correct position in the "items" list
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val item = items[bindingAdapterPosition]
                    if (!multiSelectMode) {
                        onItemClick(item)
                    } else {
                        toggleSelection(item)
                    }
                }
            }

            /** Long click → enter multi-select mode */
            view.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemLongClick(items[bindingAdapterPosition])
                }
                true
            }

            /** CheckBox click */
            checkBox.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val item = items[bindingAdapterPosition]
                    toggleSelection(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Load icon
        val comp = ComponentName(item.packageName, item.activityName)
        val drawable: Drawable? = try {
            pm.getActivityIcon(comp)
        } catch (_: Exception) {
            null
        }

        holder.icon.setImageDrawable(drawable)
        holder.name.text = item.label.ifEmpty { item.activityName }
        holder.pkg.text = "${item.packageName}\n${item.activityName}"

        // Checkbox visibility
        holder.checkBox.visibility = if (multiSelectMode) View.VISIBLE else View.GONE

        // Checkbox checked state
        holder.checkBox.isChecked = selectedItems.contains(item)

        // Visual feedback
        holder.itemView.alpha = if (selectedItems.contains(item)) 0.5f else 1f
    }

    override fun getItemCount(): Int = items.size

    /** Enable multi-select mode */
    fun enableMultiSelect() {
        multiSelectMode = true
        notifyDataSetChanged()
    }

    /** Disable multi-select mode */
    fun disableMultiSelect() {
        multiSelectMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    /** Toggle one item selection */
    fun toggleSelection(item: RecentItem) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        // (FIX) Only update the clicked item for better performance
        notifyItemChanged(items.indexOf(item))
        onSelectionChanged()
    }

    /** Number of selected items */
    fun getSelectedCount(): Int = selectedItems.size

    /** Return selected items */
    fun getSelectedItems(): List<RecentItem> = selectedItems.toList()

    /** Replace entire list */
    fun submitList(list: MutableList<RecentItem>) {
        // (MODIFIED) When the list is updated (e.g., after deletion), update both the original and display lists
        originalList = list.toMutableList()
        items = list.toMutableList()
        notifyDataSetChanged()
    }

    /** (NEW) Filter the list based on a keyword */
    fun filter(query: String?) {
        val q = query?.lowercase()?.trim()
        if (q.isNullOrEmpty()) {
            // No search -> restore the original list
            items = originalList.toMutableList()
        } else {
            // Has search -> filter from the "original list"
            items = originalList.filter {
                it.label.lowercase().contains(q) ||
                        it.activityName.lowercase().contains(q) ||
                        it.packageName.lowercase().contains(q)
            }.toMutableList()
        }
        // (FIX) Use DiffUtil or at least notifyDataSetChanged() to update the list
        notifyDataSetChanged()
    }
}