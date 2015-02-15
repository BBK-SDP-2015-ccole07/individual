package sml;

/**
 * This class ....
 * 
 * @author Peter
 */

public class BnzInstruction extends Instruction {

	private int op1;
	private String op2;

	public BnzInstruction(String label, String op) {
		super(label, op);
	}

	public BnzInstruction(String label, int op1, String op2) {
		this(label, "bnz");
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public void execute(Machine m) {
		int value = m.getRegisters().getRegister(op1);
		if (value != 0) m.setPc(m.getLabels().indexOf(op2));
	}

	@Override
	public String toString() {
		return super.toString() + " jump to " + op2;
	}
}
