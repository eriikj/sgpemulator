package com.johnsson.erik.sgpemulator;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;

public class ROM extends Memory {
	public ROM(String filename, Context context) throws FileNotFoundException, IOException {
		super(8192, false);

		this.readFromFile(filename, context);
	}
}
