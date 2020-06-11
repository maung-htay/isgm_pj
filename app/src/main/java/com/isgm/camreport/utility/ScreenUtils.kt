package com.isgm.camreport.utility

import android.app.Activity
import android.app.Service
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Screen utils class for app
 *
 * @author ZiSarKNar on 6/6/2020
 */
class ScreenUtils private constructor() {

    var screenWidthPx = 0f
        private set
    var screenHeightPx = 0f
        private set
    var screenDensity = 0f
        private set

    fun initScreenDimension(activity: Activity) {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        screenDensity = activity.resources.displayMetrics.density
        screenWidthPx = outMetrics.widthPixels.toFloat()
        screenHeightPx = outMetrics.heightPixels.toFloat()
    }

    companion object {
        const val SCALE_START_ANCHOR = 0.3f
        const val SCALE_DELAY = 200
        const val IMMEDIATELY = 0
        private var objInstance: ScreenUtils? = null
        val instance: ScreenUtils?
            get() {
                if (objInstance == null) {
                    objInstance = ScreenUtils()
                }
                return objInstance
            }

        /**
         * Put the content below the status bar and make the status bar translucent.
         *
         * @param activity
         */
        fun setStatusBarTranslucent(isToTranslucent: Boolean, activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (isToTranslucent) {
                    val window = activity.window
                    window.setFlags(
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else {
                    val attrs = activity.window
                            .attributes
                    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
                    activity.window.attributes = attrs
                    activity.window.clearFlags(
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                }
            }
        }

        fun setStatusBarColor(colorReference: Int, activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                // finally change the color
                window.statusBarColor = activity.resources.getColor(colorReference)
            }
        }

        /**
         * Show soft keyboard in some situation.
         */
        fun showSoftKeyboard(context: Context) {
            val imm = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }

        /**
         * Hide soft keyboard based on the EditText which the focus is in.
         *
         * @param etList
         */
        fun hideSoftKeyboard(context: Context, vararg etList: EditText) {
            val imm = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm != null) {
                for (et in etList) {
                    imm.hideSoftInputFromWindow(et.applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }

        /**
         * Get pixel from dpi.
         *
         * @param dpi
         * @return
         */
        fun getPixelFromDPI(context: Context, dpi: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi, context.resources.displayMetrics)
        }
    }
}