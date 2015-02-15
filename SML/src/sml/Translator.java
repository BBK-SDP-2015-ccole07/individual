package sml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/*
 * The translator of a <b>S</b><b>M</b>al<b>L</b> program.
 */
public class Translator {

	// word + line is the part of the current line that's not yet processed
	// word has no whitespace
	// If word and line are not empty, line begins with whitespace
	private String line = "";
	private Labels labels; // The labels of the program being translated
	private ArrayList<Instruction> program; // The program to be created
	private String fileName; // source file of SML code

	private static final String SRC = "src";

	public Translator(String fileName) {
		this.fileName = SRC + "/" + fileName;
	}

	// translate the small program in the file into lab (the labels) and
	// prog (the program)
	// return "no errors were detected"
	public boolean readAndTranslate(Labels lab, ArrayList<Instruction> prog) {

		try (Scanner sc = new Scanner(new File(fileName))) {
			// Scanner attached to the file chosen by the user
			labels = lab;
			labels.reset();
			program = prog;
			program.clear();

			try {
				line = sc.nextLine();
			} catch (NoSuchElementException ioE) {
				return false;
			}

			// Each iteration processes line and reads the next line into line
			while (line != null) {
				// Store the label in label
				String label = scan();

				if (label.length() > 0) {
					Instruction ins = getInstruction(label);
					if (ins != null) {
						labels.addLabel(label);
						program.add(ins);
					}
				}

				try {
					line = sc.nextLine();
				} catch (NoSuchElementException ioE) {
					return false;
				}
			}
		} catch (IOException ioE) {
			System.out.println("File: IO error " + ioE.getMessage());
			return false;
		}
		return true;
	}

	// Each line consists of an MML instruction, with its label already removed.
	// It is then translated an instruction with label label and the instruction is returned
	public Instruction getInstruction(String label) {
		Class<?> c;				// the instruction class
		Constructor[] con;		// an array of constructors for this class
		Class<?>[] paramType;	// the parameter types for the constructor we want to invoke 

		if (line.equals(""))
			return null;

		// get the instruction and use it to construct the class name
		String ins = scan();
		String className = "sml." + ins.substring(0, 1).toUpperCase() + ins.substring(1) + "Instruction";

		try {
			// get the class ...
			c = Class.forName(className);
			
			// ... and then all of its constructors ...
			con = c.getConstructors();
			
			// .. and then the parameter types expected by the constructor declared 2nd
			// There's a slightly dodgy assumption here that any future instructions will
			// have their constructor declared in the same order.
			paramType = con[1].getParameterTypes();
			
			// Local var to hold the number of parameters this instruction is expecting
			int numParams = paramType.length;
			
			// Local var to hold those parameters, which we'll now go get
			Object[] param = new Object[paramType.length];
			
			// We already have the first parameter, so we'll not waste time on that
			param[0] = label;
			
			// For the rest we iterate through the expected parameter list,
			// casting the inputs to int where appropriate and trapping
			// missing parameters
		    for (int i = 1; i < numParams; i++) {
		    	if (paramType[i].equals(int.class)) {
		    		param[i] = scanInt();
		    		if (param[i].equals(Integer.MAX_VALUE))
		    			throw new IllegalArgumentException();
		    	} else {
		    		param[i] = (String) scan();
		    		if (param[i].equals(""))
		    			throw new IllegalArgumentException();
		    	}
		    }

		    // Check that we don't have extra, unexpected parameters
		    if (!scan().equals(""))
		    	throw new IllegalArgumentException();
		    
		    // Finally we invoke that "2nd" constructor with the params that we've
		    // carefully collated for it
		    return (Instruction) con[1].newInstance(param);
		
		} catch (IllegalArgumentException e) {
		    error("Too few, too many or wrong parameter type", label);
		} catch (ClassNotFoundException | InstantiationException | InvocationTargetException e) {
			error("Unable to carry out instruction", label);
		} catch (IllegalAccessException | SecurityException e) {
			error("Access or security violation", label);	
		}
		
		// We only get this far if something has gone wrong
		return null;
	}

	/*
	 * Return the first word of line and remove it from line. If there is no
	 * word, return ""
	 */
	private String scan() {
		line = line.trim();
		if (line.length() == 0)
			return "";

		int i = 0;
		while (i < line.length() && line.charAt(i) != ' ' && line.charAt(i) != '\t') {
			i = i + 1;
		}
		String word = line.substring(0, i);
		line = line.substring(i);
		return word;
	}

	// Return the first word of line as an integer. If there is
	// any error, return the maximum int
	private int scanInt() {
		String word = scan();
		if (word.length() == 0) {
			return Integer.MAX_VALUE;
		}

		try {
			return Integer.parseInt(word);
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}
	
	private void error(String msg, String label) {
		System.out.println("Parse Error: " + msg + " on line " + label);
	}
}