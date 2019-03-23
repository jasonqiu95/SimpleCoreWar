
public final class Redcode {

    public static final Redcode NO_OP = new Redcode(Operator.NOP, 0, null, 0, null, -1, -1);

    public enum Operator {
        MOV, DATA, JMP, ADD, SPL, NOP;
    }

    public enum AddressingMode {
        DIRECT, A_INDIRECT, B_INDIRECT, IMMEDIATE;
    }

    Operator op;
    int aField;
    AddressingMode aFieldAddrMode;
    int bField;
    AddressingMode bFieldAddrMode;
    int programID;
    int processID;

    public Redcode(Operator op, int aField, AddressingMode aFieldAddrMode, int bField, AddressingMode bFieldAddrMode, int programID, int processID) {
        this.op = op;
        this.aField = aField;
        this.aFieldAddrMode = aFieldAddrMode;
        this.bField = bField;
        this.bFieldAddrMode = bFieldAddrMode;
        this.programID = programID;
        this.processID = processID;
    }

    public Redcode(Redcode code) {
        this.op = code.op;
        this.aField = code.aField;
        this.aFieldAddrMode = code.aFieldAddrMode;
        this.bField = code.bField;
        this.bFieldAddrMode = code.bFieldAddrMode;
        this.programID = code.programID;
        this.processID = code.processID;
    }

    public int[] getFields() {
        return new int[] {aField, bField};
    }

    public void setFields(int[] fields) {
        this.aField = fields[0];
        this.bField = fields[1];
    }

}
