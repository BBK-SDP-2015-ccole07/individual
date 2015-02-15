package sml;

/**
 * This class ....
 * 
 * @author Peter
 */

public class OutInstruction extends Instruction {

	private int op;

	public OutInstruction(String label, String op) {
		super(label, op);
	}

	public OutInstruction(String label, int op) {
		this(label, "out");
		this.op = op;
	}

	@Override
	public void execute(Machine m) {
		int value = m.getRegisters().getRegister(op);
		System.out.println(value);
	}

	@Override
	public String toString() {
		return super.toString() + " " + op;
	}
}
