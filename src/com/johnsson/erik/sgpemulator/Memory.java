package com.johnsson.erik.sgpemulator;

import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;

public class Memory {
	public class MemoryWriteProtectedException extends Exception {
		private static final long serialVersionUID = 1216724011750537065L;
	}

	public class MemoryAccessOutOfBoundsException extends Exception {
		private static final long serialVersionUID = -4175862435503574779L;
	}

	private int size;
	private boolean writeable;

	private byte data[];

	public Memory(int size, boolean writeable) {
		this.size = size;
		this.writeable = writeable;

		data = new byte[size];
	}

	void readFromFile (String filename, Context context) throws IOException {
		FileInputStream fileHandle = new FileInputStream(filename);

		fileHandle.read(data);

		fileHandle.close();
	}

	void write (int address, byte data) throws MemoryWriteProtectedException, MemoryAccessOutOfBoundsException {
		if (!writeable) {
			throw new MemoryWriteProtectedException();
		}

		if (address >= size || address < 0) {
			System.out.println("Hej: " + address);

			throw new MemoryAccessOutOfBoundsException();
		}

		this.data[address] = data;
	}

	byte read (int address) throws MemoryAccessOutOfBoundsException {
		if (address >= size || address < 0) {
			throw new MemoryAccessOutOfBoundsException();
		}

		return data[address];
	}
}
