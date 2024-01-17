package com.otp.otpindia.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarUtils {
    fun showSnackBar(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(view, message, duration).show()
    }
}