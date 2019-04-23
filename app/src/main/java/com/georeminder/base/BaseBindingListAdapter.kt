package com.georeminder.base

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.databinding.ViewDataBinding
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


//Created by imobdev-rujul on 23/11/18
abstract class BaseBindingListAdapter<T>(diffUtil: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, BaseBindingViewHolder>(diffUtil),
        BaseBindingViewHolder.ClickListener {

    override fun onViewClick(view: View, position: Int) {
        itemClickListener?.onItemClick(view, getItem(position)!!, position)
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

    fun getItemsArray(): ArrayList<T> {
        return items
    }

    var itemClickListener: ItemClickListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder {
        val inflater = LayoutInflater.from(parent.getContext())
        val binding = bind(inflater, parent, viewType)
        return BaseBindingViewHolder(binding, this)
    }

    protected abstract fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding

    interface ItemClickListener<T> {
        fun onItemClick(view: View, data: T, position: Int)
    }

}