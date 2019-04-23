package com.georeminder.ui.dialogs

import android.content.Context
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.georeminder.utils.onChange
import kotlinx.android.synthetic.main.dialog_list.*

/**
 * Created by Darshna Desai on 31/5/18.
 */


/**
 * A common class which provides a bottom sheet dialog with recycler view
 * and search to filter feature.
 */
class ListDialog(context: Context, @StyleRes theme: Int) : BottomSheetDialog(context) {

    private var title: String = ""
    private var isSearchable: Boolean = false
    private var dialogListener: OnItemClick? = null
    private var data = ArrayList<Any>()

    constructor(context: Context, title: String, isSearchable: Boolean
                , data: ArrayList<Any>, dialogListener: OnItemClick) : this(context, 0) {
        this.title = title
        this.isSearchable = isSearchable
        this.data = data
        this.dialogListener = dialogListener
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        setViewProperty(context, view)
    }

    private fun setViewProperty(context: Context, contentView: View) {
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        params.setMargins(50, 0, 50, 0)
        (contentView.parent as View).setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        tvTitle.text = title
        if (!isSearchable) edtSearch.visibility = View.GONE

        rvDialog.layoutManager = LinearLayoutManager(context)
        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        rvDialog.addItemDecoration(divider)
        rvDialog.itemAnimator = DefaultItemAnimator() as RecyclerView.ItemAnimator?

        rvDialog.adapter = ListDialogSpinnerAdapter(context, data,
                object : OnItemClick {
                    override fun selectedItem(position: Int, selectedItem: Any) {
                        dismiss()
                        dialogListener?.selectedItem(position, selectedItem)
                        dismiss()
                    }
                })

        edtSearch.onChange {
            (rvDialog.adapter as ListDialogSpinnerAdapter).filter(it)
        }
    }

    interface OnItemClick {
        fun selectedItem(position: Int, selectedItem: Any)
    }

}