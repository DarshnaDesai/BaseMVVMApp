package com.georeminder.ui.dialogs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.georeminder.R
import com.georeminder.data.model.other.ReminderType


/**
 * Created by imobdev on 3/12/16.
 */

/**
 * Adapter class for showing the items of common ListDialog
 */
class ListDialogSpinnerAdapter(context: Context, private var itemList: List<Any>,
                               private val listener: ListDialog.OnItemClick)
    : RecyclerView.Adapter<ListDialogSpinnerAdapter.ViewHolder>() {

    private val mainData: List<Any> = itemList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list_dialog
                , parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (itemList[position]) {
            is String -> {
                val model = itemList[position] as String
                holder.tvTitle.text = model
            }
            is ReminderType -> {
                val model = itemList[position] as ReminderType
                holder.tvTitle.text = model.type
            }
        }
        holder.itemView.setOnClickListener { listener.selectedItem(holder.adapterPosition, itemList[holder.adapterPosition]) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }


    /**
     * A method to filter out records as per the search keyword entered by user.
     */
    fun filter(char: String) {
        itemList = if (char == "") mainData
        else {
            when {
                /*mainData[0] is Country -> (mainData as ArrayList<Country>)
                        .filter { (it.countryName).toLowerCase().startsWith(char.toLowerCase()) }*/
                else -> mainData.filter { (it as String).toLowerCase().startsWith(char.toLowerCase()) }
            }
        }
        notifyDataSetChanged()
    }
}