package me.grishka.houseclub.views

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView

class SquircleImageView : ImageView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        outlineProvider = squircleOutline
        clipToOutline = true
    }

    companion object {
        private val squircleOutline: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                if (view.width == 0 || view.height == 0) return
                //			Path path=new Path();
                //			path.moveTo(0, view.getHeight()/2f);
                //			path.cubicTo(0f, 0f, 0f, 0f, view.getWidth()/2f, 0f);
                //			path.cubicTo(view.getWidth(), 0f, view.getWidth(), 0f, view.getWidth(), view.getHeight()/2f);
                //			path.cubicTo(view.getWidth(), view.getHeight(), view.getWidth(), view.getHeight(), view.getWidth()/2f, view.getHeight());
                //			path.cubicTo(0f, view.getHeight(), 0f, view.getHeight(), 0f, view.getHeight()/2f);
                //			path.close();
                //			outline.setConvexPath(path);
                outline.setRoundRect(0, 0, view.width, view.height, view.width * 0.42f)
            }
        }
    }
}