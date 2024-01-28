package com.otp.otpindia.ui.animation

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.otp.otpindia.utils.Constants

class ProgressBarAnimation(
    private val progressBar: ProgressBar,
    private val leafImageView: ImageView,
    private val from: Int,
    private val to: Int,
    private val listener: OnProgressListener? = null
) : Animation() {
    interface OnProgressListener {
        fun onProgress(progress: Int)
    }
    init {
        duration = Constants.DELAY // Adjust the duration as needed
    }
    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()

        // Update leaf image position based on progress
        val progressBarWidth = progressBar.width - progressBar.paddingStart - progressBar.paddingEnd
        val leafPosition = (progressBarWidth * interpolatedTime).toInt()
        updateLeafPosition(leafPosition)

        listener?.onProgress(value.toInt())
    }

    private fun updateLeafPosition(leafPosition: Int) {
        val layoutParams = leafImageView.layoutParams as FrameLayout.LayoutParams
        layoutParams.marginStart = leafPosition
        leafImageView.layoutParams = layoutParams
    }
}
