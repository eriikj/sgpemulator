package com.johnsson.erik.sgpemulator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SGPScreen extends SurfaceView {
	Bitmap b;
    Paint paint;
    SurfaceHolder holder;

    public SGPScreen(Context context, AttributeSet attrs) {
		super(context, attrs);

		paint = new Paint();
		
		holder = this.getHolder();
	}

	public void doDraw(Canvas canvas) {
		paint.setColor(Color.GREEN);

		canvas.drawRect(20, 20, 30, 30, paint);
	}

	public void sync() {
		Toast.makeText(getContext(), "Test!", Toast.LENGTH_SHORT).show();

		Canvas canvas = holder.lockCanvas();

		this.doDraw(canvas);

		holder.unlockCanvasAndPost(canvas);
	}
}
