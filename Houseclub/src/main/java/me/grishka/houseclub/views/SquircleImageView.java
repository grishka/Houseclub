package me.grishka.houseclub.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import me.grishka.appkit.utils.V;

public class SquircleImageView extends ImageView {

    private Path clipPath, borderPath = new Path();
    private Paint clipPaint, borderPaint;

    public SquircleImageView(Context context) {
        super(context);
        init();
    }

    public SquircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SquircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Why not clipToOutline? Because clipping isn't supported for arbitrary paths ¯\_(ツ)_/¯
        setScaleType(ScaleType.CENTER_CROP);
        setLayerType(LAYER_TYPE_HARDWARE, null); // important so that CLEAR xfermode doesn't put a hole through the entire window
        clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clipPaint.setColor(0xFF000000);
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0x20000000);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clipPath = new Path();
        clipPath.moveTo(0.0f, 100.0f);
        clipPath.cubicTo(0.0f, 33.0f, 33.0f, 0.0f, 100.0f, 0.0f);
        clipPath.cubicTo(167.0f, 0.0f, 200.0f, 33.0f, 200.0f, 100.0f);
        clipPath.cubicTo(200.0f, 167.0f, 167.0f, 200.0f, 100.0f, 200.0f);
        clipPath.cubicTo(33.0f, 200.0f, 0.0f, 167.0f, 0.0f, 100.0f);
        clipPath.close();

        Matrix m = new Matrix();
        m.setScale(w / 200f, h / 200f, 0f, 0f);
        clipPath.transform(m);

        m.setScale((float) (w - V.dp(1)) / w, (float) (w - V.dp(1)) / h, w / 2f, h / 2f);
        clipPath.transform(m, borderPath);

        clipPath.toggleInverseFillType();
        borderPath.toggleInverseFillType();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (clipPath != null) {
            canvas.drawPath(borderPath, borderPaint);
            canvas.drawPath(clipPath, clipPaint);
        }
    }
}
