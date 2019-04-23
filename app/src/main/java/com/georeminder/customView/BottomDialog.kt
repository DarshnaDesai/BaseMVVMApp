package com.georeminder.customView

import android.content.Context
import android.graphics.Rect
import android.support.annotation.StyleRes
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.view.View

class BottomDialog(context: Context, @StyleRes style: Int) : BottomSheetDialog(context, style) {

    override fun setContentView(view: View) {
        super.setContentView(view)
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        val layoutParams = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val displayRectangle = Rect()
        window?.decorView?.getWindowVisibleDisplayFrame(displayRectangle)

        layoutParams.width = (displayRectangle.width() * 0.9f).toInt()
        (view.parent as View).layoutParams = layoutParams
    }

    override fun setCancelable(cancelable: Boolean) {
        val dialog = this
        val touchOutsideView = dialog.window!!.decorView.findViewById<View>(android.support.design.R.id.touch_outside)
        val bottomSheetView = dialog.window!!.decorView.findViewById<View>(android.support.design.R.id.design_bottom_sheet)

        if (cancelable) {
            touchOutsideView.setOnClickListener {
                if (dialog.isShowing) {
                    dialog.cancel()
                }
            }
            BottomSheetBehavior.from(bottomSheetView).setHideable(true)
        } else {
            touchOutsideView.setOnClickListener(null)
            BottomSheetBehavior.from(bottomSheetView).setHideable(false)
        }
    }

}