package com.johnsson.erik.sgpemulator;

import com.johnsson.erik.sgpemulator.Memory.MemoryAccessOutOfBoundsException;
import com.johnsson.erik.sgpemulator.Memory.MemoryWriteProtectedException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class SGPScreen extends SurfaceView {
	private Paint paint;
	private SurfaceHolder holder;
	private long lastSync;
	private long delay;

	public SGPScreen(Context context, AttributeSet attrs) {
		super(context, attrs);

		paint = new Paint();
		holder = this.getHolder();
		lastSync = System.nanoTime() / 1000;
		delay = 10000;
	}

	public void drawFrame(Memory memory) throws MemoryAccessOutOfBoundsException, MemoryWriteProtectedException {
		long current;
		long change;
		int x, y, i;
		int block;

		Canvas canvas = holder.lockCanvas();

		paint.setColor(Color.BLACK);

		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

		paint.setColor(Color.WHITE);

		for (y = 0; y < 256; y++) {
			for (x = 0; x < 32; x++) {
				block = memory.read (y * 32 + x);

				memory.write (y * 32 + x, (byte)0);

				for (i = 0; i < 8; i++) {
					if ((block & 0x80) > 0) {
						canvas.drawPoint(x * 8 + i, y, paint);
					}

					block = block << 1;
				}
			}
		}

		current = System.nanoTime() / 1000;
		change = 100 * (20 - (current - lastSync));

		if (change < 10000 && change > -10000) {
		    delay += change;
		}

		if (delay < 0) {
			delay = 0;
		}

		if (delay != 0) {
			try {
				Thread.sleep(delay);
			}
			catch (InterruptedException e) {
				/* Really do nothing */
			}
		}

		lastSync = current;

		holder.unlockCanvasAndPost(canvas);
	}

	public void sync() {
		Toast.makeText(getContext(), "Test!", Toast.LENGTH_SHORT).show();
		try {
			Memory m = new Memory(8192, true);

			for (int i = 0; i < 8192; i++) {
				m.write(i, (byte)0xff);
			}

			drawFrame(m);
		} catch (MemoryAccessOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MemoryWriteProtectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
