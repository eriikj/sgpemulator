package com.johnsson.erik.sgpemulator;

import java.io.IOException;

import com.johnsson.erik.sgpemulator.Processor.ProcessorAlreadyRunningException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SGPActivity extends Activity {
	Processor processor;
	ROM rom;
	RAM ram;
	Graphic graphic;
	Input input;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getApplicationContext();
    	SGPScreen screen = (SGPScreen) findViewById(R.id.sgp_screen);

		setContentView(R.layout.activity_sgp);

		try {
			rom = new ROM(Environment.getExternalStorageDirectory() + "/sgp.sgp", context);
		}
		catch (IOException e) {
			Toast.makeText(context, R.string.error_unable_to_load + " " + Environment.getExternalStorageDirectory() + "/sgp.sgp", Toast.LENGTH_LONG).show();

			finish();

			return;
		}

		ram = new RAM();
		graphic = new Graphic(screen);
		input = new Input();

		processor = new Processor(rom, ram, graphic, input);

		try {
			processor.start();
		}
		catch (ProcessorAlreadyRunningException e) {
			Toast.makeText(context, R.string.error_processor_already_running, Toast.LENGTH_LONG).show();			
		}

/*		while (processor.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}*/

/*		Toast.makeText(context, processor.errorStringResource, Toast.LENGTH_LONG).show();

		finish();*/
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_test:
	        	SGPScreen screen = (SGPScreen) findViewById(R.id.sgp_screen);

	        	screen.sync();

	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sgp, menu);
		return true;
	}

}
