package com.georeminder.ui.dashboard.more

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.georeminder.base.BaseBindingAdapter
import com.georeminder.base.BaseBindingViewHolder
import com.georeminder.data.model.other.FriendRequest
import android.databinding.ViewDataBinding
import android.databinding.DataBindingUtil
import com.georeminder.databinding.ItemBlockedListBinding


/**
 * Created by Darshna Desai on 18/2/19.
 */
class BlockedListAdapter(context: Context) : BaseBindingAdapter<FriendRequest>() {

    var context: Context? = context

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemBlockedListBinding.inflate(LayoutInflater.from(context), parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, pos: Int) {
        val binding = DataBindingUtil.getBinding<ItemBlockedListBinding>(holder.itemView)
        binding?.friendData = items[pos]
        binding?.btnUnBlock?.setOnClickListener { onViewClick(binding.btnUnBlock, holder.adapterPosition) }
    }

}