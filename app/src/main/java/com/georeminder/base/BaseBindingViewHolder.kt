package com.georeminder.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

//Created by imobdev-rujul on 23/11/18
class BaseBindingViewHolder : RecyclerView.ViewHolder, View.OnClickListener {
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View?) {
        clickListener.onViewClick(v!!, adapterPosition)
    }

    var binding: ViewDataBinding
    var clickListener: ClickListener

    constructor(binding: ViewDataBinding, clickListener: ClickListener) : super(binding.root) {
        binding.root.setOnClickListener(this)
        this.binding = binding
        this.clickListener = clickListener
        clickViews(this)
    }

    private fun clickViews(bindingViewHolder: BaseBindingViewHolder) {
        val view = bindingViewHolder.binding.root
        if (view is ViewGroup) {
            setViewGroupClick(view, view)
        }
    }

    private fun setViewGroupClick(viewGroup: ViewGroup, parentView: View) {
        for (i in 0 until viewGroup.childCount) {
            if (viewGroup.childCount > 0 && viewGroup.getChildAt(i) is ViewGroup) {
                setViewGroupClick(viewGroup.getChildAt(i) as ViewGroup, parentView)
            }
            viewGroup.getChildAt(i).setOnClickListener { view -> clickListener.onViewClick(view!!, adapterPosition) }
        }
    }

    interface ClickListener {
        fun onViewClick(view: View, position: Int)
    }
}