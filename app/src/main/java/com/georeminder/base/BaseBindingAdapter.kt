package com.georeminder.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

//Created by imobdev-rujul on 23/11/18
abstract class BaseBindingAdapter<T> : RecyclerView.Adapter<BaseBindingViewHolder>(), BaseBindingViewHolder.ClickListener {

    override fun onViewClick(view: View, position: Int) {
        itemClickListener?.onItemClick(view, items[position], position)
    }

    /**
     * Enable filter or not !
     */
    var filterable: Boolean = false

    protected var items: ArrayList<T> = ArrayList<T>()

    /**
     * used for backup purpose in case of filterable = true
     */
    protected var allItems: ArrayList<T> = ArrayList<T>()

    var itemClickListener: ItemClickListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding = bind(inflater, parent, viewType)
        return BaseBindingViewHolder(binding, this)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(item: ArrayList<T>) {
        items = item
        if (filterable) allItems = item
        notifyDataSetChanged()
    }

    fun addItems(item: ArrayList<T>) {
        items.addAll(item)
        if (filterable) allItems.addAll(item)
        notifyDataSetChanged()
    }

    fun addItem(item: T) {
        items.add(item)
        if (filterable) allItems.add(item)
        notifyDataSetChanged()
    }

    fun addItemNotify(item: T) {
        items.add(item)
        if (filterable) allItems.add(item)
        notifyItemInserted(itemCount-1)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        if (filterable) allItems.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clear() {
        val size = items.size
        if (size > 0) {
            items.clear()
            if (filterable) allItems.clear()
            notifyItemRangeRemoved(0, size)
        }
    }

    protected abstract fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding

    interface ItemClickListener<T> {
        fun onItemClick(view: View, data: T, position: Int)
    }
}