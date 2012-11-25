package com.johnsson.erik.sgpemulator;

public class Input {
	final static int JOY1_UP = 0;
	final static int JOY1_DOWN = 1;
	final static int JOY1_LEFT = 2;
	final static int JOY1_RIGHT = 3;
	final static int JOY1_BUTTON = 4;
	final static int JOY2_UP = 5;
	final static int JOY2_DOWN = 6;
	final static int JOY2_LEFT = 7;
	final static int JOY2_RIGHT = 8;
	final static int JOY2_BUTTON = 9;

	final static int KEY_RELEASED = 0;
	final static int KEY_PRESSED = 1;

	int joystate[];

	public Input() {
		joystate = new int[10];

		joystate[JOY1_UP] = joystate[JOY1_DOWN] = joystate[JOY1_LEFT] =
				joystate[JOY1_RIGHT] = joystate[JOY1_BUTTON] = joystate[JOY2_UP] =
			    joystate[JOY2_DOWN] = joystate[JOY2_LEFT] = joystate[JOY2_RIGHT] =
			    joystate[JOY2_BUTTON] = KEY_RELEASED;
	}

	int getBit (byte in)
	{
	  	if (joystate[in] == KEY_RELEASED) {
	  		return 1;
	  	} else {
	  		return 0;
	  	}
	}

	void event (Event event) {
		joystate[event.key] = event.action;
	}
}
