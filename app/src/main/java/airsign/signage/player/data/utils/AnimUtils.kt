package airsign.signage.player.data.utils

import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.util.Log

object AnimUtils {
    private const val TAG = "AnimUtils"

    fun rotateView(value: Int, frameLayout: View, windowManager: WindowManager) {
        Log.d(TAG, "rotateView called with value: $value")
        
        // Handle default value (-1) and invalid values
        if (value == -1) {
            Log.d(TAG, "Rotation value is -1 (default), skipping rotation")
            return
        }
        
        if (value < 0 || value > 360) {
            Log.w(TAG, "Invalid rotation value: $value, must be between 0-360")
            return
        }

        val rot = value.toFloat()
        Log.d(TAG, "Applying rotation: $rot degrees")
        
        try {
            if (rot == 90f || rot == 270f) {
                // For 90° and 270° rotations, we need to swap dimensions
                val displayMetrics = DisplayMetrics()
                
                // Use modern API to get display metrics
                val context = frameLayout.context
                val display = context.display
                if (display != null) {
                    display.getMetrics(displayMetrics)
                } else {
                    // Fallback to deprecated API for older devices
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay?.getMetrics(displayMetrics)
                }
                
                val height = displayMetrics.heightPixels
                val width = displayMetrics.widthPixels
                val offset = (width - height) / 2
                
                Log.d(TAG, "Display dimensions: ${width}x${height}, offset: $offset")
                
                val lp = FrameLayout.LayoutParams(height, width)
                frameLayout.layoutParams = lp
                frameLayout.rotation = rot
                frameLayout.translationX = offset.toFloat()
                frameLayout.translationY = -offset.toFloat()
                
                Log.d(TAG, "Applied 90°/270° rotation with dimension swap")
            } else {
                // For other rotations, just apply the rotation
                frameLayout.rotation = rot
                Log.d(TAG, "Applied simple rotation: $rot degrees")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying rotation: $value", e)
        }
    }

    /**
     * Alternative rotation method that doesn't swap dimensions
     */
    fun rotateViewSimple(value: Int, frameLayout: View) {
        Log.d(TAG, "rotateViewSimple called with value: $value")
        
        if (value == -1) {
            Log.d(TAG, "Rotation value is -1 (default), skipping rotation")
            return
        }
        
        if (value < 0 || value > 360) {
            Log.w(TAG, "Invalid rotation value: $value, must be between 0-360")
            return
        }

        try {
            frameLayout.rotation = value.toFloat()
            Log.d(TAG, "Applied simple rotation: ${value} degrees")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying simple rotation: $value", e)
        }
    }
}