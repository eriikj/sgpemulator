package com.johnsson.erik.sgpemulator;

import com.johnsson.erik.sgpemulator.Memory.MemoryAccessOutOfBoundsException;
import com.johnsson.erik.sgpemulator.Memory.MemoryWriteProtectedException;

public class Graphic {
	private final static int GRAPHMEM_SIZE = 8192;

	private Memory memory[];
	private SGPScreen screen;
	private int active;

	public Graphic (SGPScreen screen) {
		this.screen = screen;

		memory = new Memory[2];

		memory[0] = new Memory(GRAPHMEM_SIZE, true);
		memory[1] = new Memory(GRAPHMEM_SIZE, true);

		reset();
	}

	void putPixel (byte x, byte y) throws MemoryAccessOutOfBoundsException, MemoryWriteProtectedException {
		int data;
		
		data = memory[active].read(y * 32 + (x >> 3));

		switch (x & 0x07) {
	    case 0:
	    	data = data | 0x80;

	    	break;
	    case 1:
	    	data = data | 0x40;

	    	break;
	    case 2:
	    	data = data | 0x20;

	    	break;
	    case 3:
	    	data = data | 0x10;

	    	break;
	    case 4:
	    	data = data | 0x08;

	    	break;
	    case 5:
	    	data = data | 0x04;

	    	break;
	    case 6:
	    	data = data | 0x02;

	    	break;
	    case 7:
	    	data = data | 0x01;

	    	break;
	    }

		memory[active].write(y * 32 + (x >> 3), (byte)data);
	}

	void sync () throws MemoryAccessOutOfBoundsException, MemoryWriteProtectedException {
		screen.drawFrame(memory[active]);

		if (active == 1) {
			active = 0;
		} else {
			active = 1;
		}
	}

	void reset () {
	  active = 0;
	}
}
