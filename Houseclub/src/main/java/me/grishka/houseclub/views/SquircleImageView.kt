package me.grishka.houseclub.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import me.grishka.appkit.utils.V;

public class SquircleImageView extends ImageView{

	private static final ViewOutlineProvider squircleOutline=new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			if(view.getWidth()==0 || view.getHeight()==0)
				return;
//			Path path=new Path();
//			path.moveTo(0, view.getHeight()/2f);
//			path.cubicTo(0f, 0f, 0f, 0f, view.getWidth()/2f, 0f);
//			path.cubicTo(view.getWidth(), 0f, view.getWidth(), 0f, view.getWidth(), view.getHeight()/2f);
//			path.cubicTo(view.getWidth(), view.getHeight(), view.getWidth(), view.getHeight(), view.getWidth()/2f, view.getHeight());
//			path.cubicTo(0f, view.getHeight(), 0f, view.getHeight(), 0f, view.getHeight()/2f);
//			path.close();
//			outline.setConvexPath(path);
			outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getWidth()*0.42f);
		}
	};

	public SquircleImageView(Context context){
		super(context);
		init();
	}

	public SquircleImageView(Context context, @Nullable AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public SquircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init(){
		setOutlineProvider(squircleOutline);
		setClipToOutline(true);
	}
}
