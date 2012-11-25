package com.johnsson.erik.sgpemulator;

import com.johnsson.erik.sgpemulator.Memory.MemoryAccessOutOfBoundsException;
import com.johnsson.erik.sgpemulator.Memory.MemoryWriteProtectedException;

public class Processor {
	public class IllegalInstructionException extends Exception {
		private static final long serialVersionUID = 8501853451356949675L;
	}

	public class ProcessorAlreadyRunningException extends Exception {
		private static final long serialVersionUID = -5440254233295236446L;
	}

	private final static int HLT = 0x00;

	private final static int LDAv = 0x10;
	private final static int LDAa = 0x11;
	private final static int STA = 0x12;
	private final static int EXG = 0x13;
	private final static int EXX = 0x14;

	private final static int SLC = 0x15;
	private final static int SRC = 0x16;

	private final static int SLL = 0x17;
	private final static int SRL = 0x18;

	private final static int ROL = 0x19;
	private final static int ROR = 0x1a;

	private final static int ADDv = 0x20;
	private final static int ADDa = 0x21;
	private final static int ADCv = 0x22;
	private final static int ADCa = 0x23;

	private final static int SUBv = 0x24;
	private final static int SUBa = 0x25;
	private final static int SBCv = 0x26;
	private final static int SBCa = 0x27;

	private final static int CMPv = 0x28;
	private final static int CMPa = 0x29;

	private final static int ANDv = 0x2a;
	private final static int ANDa = 0x2b;

	private final static int ORv = 0x2c;
	private final static int ORa = 0x2d;

	private final static int XORv = 0x2e;
	private final static int XORa = 0x2f;

	private final static int JMP = 0x30;
	private final static int JMPZ = 0x31;
	private final static int JMPC = 0x32;

	private final static int DRW = 0x33;
	private final static int SYNC = 0x34;

	private final static int INZ = 0x35;

	private ROM rom;
	private RAM ram;
	private Graphic graphic;
	private Input input;

	private int programCounter;
	private long tics;

	private byte registerA;
	private byte registerX;
	private byte registerG;
	private byte flagZ;
	private byte flagC;

	private Thread thread;

	public int errorStringResource;
	public boolean error;
	public boolean running;

	public Processor(ROM rom, RAM ram, Graphic graphic, Input input) {
		this.rom = rom;
		this.ram = ram;
		this.graphic = graphic;
		this.input = input;
		this.running = false;
		this.error = false;

		reset();
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized boolean hasFailed() {
		return error;
	}

	private synchronized void setRunning(boolean running) {
		this.running = running;
	}

	private synchronized void setError(boolean error) {
		this.error = error;
	}

	public synchronized void start() throws ProcessorAlreadyRunningException {
		if (isRunning()) {
			throw new ProcessorAlreadyRunningException();
		}

		setRunning(true);
		setError(false);

		thread = new ProcessorThread();

		thread.start();
	}

	public synchronized void halt() {
		setRunning(false);
	}

	public synchronized void reset() {
		programCounter = 0;
		tics = 0;
	}

	private synchronized void print () {
		byte instruction, data, memdata, relative_data;

		try {
			instruction = rom.read (programCounter * 2);
			data = rom.read (programCounter * 2 + 1);
			memdata = ram.read (calculateAddress(registerX, data));
		} catch (MemoryAccessOutOfBoundsException e) {
			return;
		}

		if ((data & 0x01) == 0)
			relative_data = (byte)(2 * (programCounter + (data >> 1) + 1));
		else
			relative_data = (byte)(2 * (programCounter - (data >> 1) + 1));
		
		System.out.printf("PC:\t0x%04x\nTics:\t%08d\nA:\t0x%02x\nX:\t0x%02x\nG:\t0x%02x\nZ:\t0x%02x\nC:\t0x%02x\nNext instruction:\t0x%02x%02x\t%s\n",
				programCounter * 2,
				tics,
				registerA,
				registerX,
				registerG,
				flagZ,
				flagC,
				instruction,
				data,
				disassembleInstruction (instruction, data, memdata, relative_data));
	}

	private int calculateAddress(byte high, byte low) {
		int high_int = high;
		int low_int = low;

		if (high_int < 0) {
			high_int += 256;
		}

		if (low_int < 0) {
			low_int += 256;
		}

		return (high_int << 5) | (low_int & 0x1F);
	}

	private synchronized void step () throws IllegalInstructionException, MemoryAccessOutOfBoundsException, MemoryWriteProtectedException {
		byte instruction, data;
		byte temp, temp2;

		instruction = rom.read (programCounter * 2);
		data = rom.read (programCounter * 2 + 1);

		programCounter++;

		//		System.out.println(String.format("%8x:\t0x%2x\t0x%2x   0x%2x\n", programCounter, instruction, data, registerX));
		print();

		switch (instruction) {
		case HLT:
			tics++;

			break;

		case LDAv:
			registerA = data;
			tics++;

			break;

		case LDAa:
			registerA = ram.read (calculateAddress(registerX, data));
			tics++;

			break;

		case STA:
			//			System.out.println("asd: " + registerX + " " + data + " " + ((registerX << 5) | (data & 0x1F)));
			ram.write (calculateAddress(registerX, data), registerA);
			tics += 2;

			break;

		case EXG:
			temp = registerA;

			registerA = registerG;
			registerG = temp;

			tics++;

			break;

		case EXX:
			temp = registerA;

			registerA = registerX;
			registerX = temp;

			tics++;

			break;

		case SLC:
			if ((registerA & 0x80) > 0)
				temp = 1;
			else
				temp = 0;

			registerA = (byte)((registerA << 1) | (flagC & 0x01));

			flagC = temp;

			tics++;

			break;

		case SRC:
			if ((registerA & 0x01) > 0)
				temp = 1;
			else
				temp = 0;

			registerA = (byte)(((registerA >> 1) & 0x7f) | (flagC << 7));

			flagC = temp;

			tics++;

			break;

		case SLL:
			if ((registerA & 0x80) > 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)(registerA << 1);

			tics++;

			break;

		case SRL:
			if ((registerA & 0x01) > 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)((registerA >> 1) & 0x7f);

			tics++;

			break;

		case ROL:
			if ((registerA & 0x80) < 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)((registerA << 1) | (flagC & 0x01));

			tics++;

			break;

		case ROR:
			if ((registerA & 0x01) > 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)((registerA >> 1) | (flagC << 7));

			tics++;

			break;

		case ADDv:
			if (registerA + data > 255)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)(registerA + data);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case ADDa:
			temp2 = ram.read (calculateAddress(registerX, data));

			if (registerA + temp2 > 255)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)(registerA + temp2);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 3;

			break;

		case ADCv:
			if (registerA + data + flagC > 255)
				temp = 1;
			else
				temp = 0;

			registerA = (byte)(registerA + data + flagC);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			flagC = temp;

			tics += 2;

			break;

		case ADCa:
			temp2 = ram.read (calculateAddress(registerX, data));

			if (registerA + temp2 + flagC > 255)
				temp = 1;
			else
				temp = 0;

			registerA =	(byte)(registerA + temp2 + flagC);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			flagC = temp;

			tics += 3;

			break;

		case SUBv:
			if (registerA - data < 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)(registerA - data);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case SUBa:
			temp2 = ram.read (calculateAddress(registerX, data));

			if (registerA - temp2 < 0)
				flagC = 1;
			else
				flagC = 0;

			registerA = (byte)(registerA - temp2);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 3;

			break;

		case SBCv:
			if (registerA - data - flagC < 0)
				temp = 1;
			else
				temp = 0;

			registerA = (byte)(registerA - data - flagC);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			flagC = temp;

			tics += 2;

			break;

		case SBCa:
			temp2 = ram.read (calculateAddress(registerX, data));

			if (registerA - temp2 - flagC < 0)
				temp = 1;
			else
				temp = 0;

			registerA =	(byte)(registerA - temp2 - flagC);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			flagC = temp;

			tics += 3;

			break;

		case CMPv:
			if (registerA - data < 0)
				flagC = 1;
			else
				flagC = 0;

			if (registerA - data == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics ++;

			break;

		case CMPa:
			temp2 = ram.read (calculateAddress(registerX, data));

			if (registerA - temp2 < 0)
				flagC = 1;
			else
				flagC = 0;

			if (registerA - temp2 == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case ANDv:
			registerA = (byte)(registerA & data);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case ANDa:
			temp2 = ram.read (calculateAddress(registerX, data));

			registerA = (byte)(registerA & temp2);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 3;

			break;

		case ORv:
			registerA = (byte)(registerA | data);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case ORa:
			temp2 = ram.read (calculateAddress(registerX, data));

			registerA = (byte)(registerA | temp2);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 3;

			break;

		case XORv:
			registerA = (byte)(registerA ^ data);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 2;

			break;

		case XORa:
			temp2 = ram.read (calculateAddress(registerX, data));

			registerA = (byte)(registerA ^ temp2);

			if (registerA == 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics += 3;

			break;

		case JMP:
			if ((data & 0x01) == 0)
				programCounter += (data >> 1);
			else
				programCounter -= (data >> 1);

			tics++;

			break;

		case JMPZ:
			if (flagZ > 0)
			{
				if ((data & 0x01) == 0)
					programCounter += (data >> 1);
				else
					programCounter -= (data >> 1);
			}

			tics++;

			break;

		case JMPC:
			if (flagC > 0)
			{
				if ((data & 0x01) == 0)
					programCounter += (data >> 1);
				else
					programCounter -= (data >> 1);
			}

			tics++;

			break;

		case DRW:
			graphic.putPixel (registerA, registerG);

			tics += 4;

			break;

		case SYNC:
			System.out.println("SYNC!!");

			graphic.sync ();

			tics = (tics / 100000 + 1) * 100000;

			break;

		case INZ:
			if (input.getBit (data) > 0)
				flagZ = 1;
			else
				flagZ = 0;

			tics++;

			break;

		default:
			throw new IllegalInstructionException();
		}
	}

	String disassembleInstruction (byte instruction, byte data, byte memdata, byte relative_data)	{
		switch (instruction) {
		case HLT:
			return String.format("HTL");
		case LDAv:
			return String.format("LDA #0x%.2x", data);
		case LDAa:
			return String.format("LDA 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case STA:
			return String.format("STA 0x%.2x", data);
		case EXG:
			return String.format("EXG");
		case EXX:
			return String.format("EXX");
		case SLC:
			return String.format("SLC");
		case SRC:
			return String.format("SRC");
		case SLL:
			return String.format("SLL");
		case SRL:
			return String.format("SRL");
		case ROL:
			return String.format("ROL");
		case ROR:
			return String.format("ROR");
		case ADDv:
			return String.format("ADD #0x%.2x", data);
		case ADDa:
			return String.format("ADD 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case ADCv:
			return String.format("ADC #0x%.2x", data);
		case ADCa:
			return String.format("ADC 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case SUBv:
			return String.format("SUB #0x%.2x", data);
		case SUBa:
			return String.format("SUB 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case SBCv:
			return String.format("SBC #0x%.2x", data);
		case SBCa:
			return String.format("SBC 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case CMPv:
			return String.format("CMP #0x%.2x", data);
		case CMPa:
			return String.format("CMP 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case ANDv:
			return String.format("AND #0x%.2x", data);
		case ANDa:
			return String.format("AND 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case ORv:
			return String.format("OR #0x%.2x", data);
		case ORa:
			return String.format("OR 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case XORv:
			return String.format("XOR #0x%.2x", data);
		case XORa:
			return String.format("XOR 0x%.2x\t; M(0x%.2x)=0x%.2x", data, data, memdata);
		case JMP:
			return String.format("JMP 0x%.4x\t", relative_data);
		case JMPZ:
			return String.format("JMPZ 0x%.4x\t", relative_data);
		case JMPC:
			return String.format("JMPC 0x%.4x\t", relative_data);
		case DRW:
			return String.format("DRW");
		case SYNC:
			return String.format("SYNC");
		case INZ:
			return String.format("INZ");
		default:
			return String.format("Unkown instruction!");
		}
	}

	private class ProcessorThread extends Thread {
		public ProcessorThread() {
			super();
		}

		public void run() {
			while(isRunning()) {
				try {
					step();
				}
				catch (MemoryAccessOutOfBoundsException e) {
					errorStringResource = R.string.error_memory_access_out_of_bounds;
					setError(true);
					halt();
				}
				catch (MemoryWriteProtectedException e) {
					errorStringResource = R.string.error_memory_write_protected;
					setError(true);
					halt();
				}
				catch (IllegalInstructionException e) {
					errorStringResource = R.string.error_illegal_instruction;
					setError(true);
					halt();
				}
			}
		}
	}
}
