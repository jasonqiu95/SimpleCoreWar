import java.util.*;

public class Simulator {
    public static final int DISPLAY_WIDTH = 20;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String[] COLORS = new String[] {
            ANSI_RED_BACKGROUND,
            ANSI_BLUE_BACKGROUND,
            ANSI_YELLOW_BACKGROUND,
            ANSI_GREEN_BACKGROUND
    };

    public static final int BUFFER_SIZE = 100;

    private Redcode[] buffer;
    private List<Program> programs;
    // idx into programs
    private int curProgramIdx;

    public Simulator() {
        this(BUFFER_SIZE);
    }

    public Simulator(int bufferSize) {
        this.buffer = new Redcode[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            buffer[i] = Redcode.NO_OP;
        }
        this.programs = new ArrayList<>();
        this.curProgramIdx = -1;  // no current program
    }

    public void start() {
        if (programs.size() <= 0) {
            throw new IllegalStateException("no runnable programs");
        }
        this.curProgramIdx = 0;
    }

    // handles initial spawn as well as SPL
    public void spawn(int programID, int processID, List<Redcode> program) {
        Random rand = new Random();
        int spawnLoc = rand.nextInt(buffer.length);
        for (int i = spawnLoc; i < spawnLoc + program.size(); i++) {
            if (getCode(i) != Redcode.NO_OP) {
                spawn(programID, processID, program);
            }
        }
        for (int i = spawnLoc; i < spawnLoc + program.size(); i++) {
            buffer[i] = program.get(i - spawnLoc);
        }
        programs.add(new Program(programID, program, spawnLoc));
    }

    /**
     *
     * @return games continues or not
     */
    public boolean step() {
        executeCode();
        curProgramIdx = (curProgramIdx+1) % programs.size();
        List<Integer> alivePrograms = getAlivePrograms();
        if (alivePrograms.size() == 1) {
            System.out.println("Game finished, winnder is program#" + alivePrograms.get(0));
            return false;
        }
        return true;
    }

    public List<Integer> getAlivePrograms() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < programs.size(); i++) {
            if (programs.get(i).alive) {
                result.add(programs.get(i).programID);
            }
        }
        return result;
    }

    public void executeCode() {
        Program curProgram = programs.get(curProgramIdx);
        // curLocation is absolute not relative
        int curLocation = curProgram.nextProcessPointer();
        Redcode code = getCode(curLocation);

        try {
            if (code.programID != curProgram.programID) {
                throw new IllegalArgumentException(String.format("code being executed (code's program id %d) is not the cur program id %d", code.programID, curProgram.programID));
            }

            switch (code.op) {
                case DATA:
                    programs.get(curProgramIdx).kill();
                    break;
                case JMP:
                    curProgram.jumpCurProcess(curLocation + code.aField);
                    break;
                case ADD:
                    // fields fetched by aField
                    int[] aFields = getFieldsWithAddrMode(curLocation, getCode(curLocation).aField, getCode(curLocation).aFieldAddrMode);
                    // fields fetched by bField
                    int[] bFields = getFieldsWithAddrMode(curLocation, getCode(curLocation).bField, getCode(curLocation).bFieldAddrMode);
                    int[] result = new int[] {aFields[0]+bFields[0], aFields[1]+bFields[1]};
                    getCodeWithAddrMode(curLocation, getCode(curLocation).bField, getCode(curLocation).bFieldAddrMode).setFields(result);
                    break;
                case MOV:
                    Redcode dest = new Redcode(getCodeWithAddrMode(curLocation, getCode(curLocation).aField, getCode(curLocation).aFieldAddrMode));
                    buffer[getLocWithAddrMode(curLocation, getCode(curLocation).bField, getCode(curLocation).bFieldAddrMode)%buffer.length] = dest;
                    break;
                case NOP:
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            programs.get(curProgramIdx).kill();
        }
    }

    private Redcode getCode(int idx) {
        return buffer[idx%buffer.length];
    }

    private int[] getFieldsWithAddrMode(int curAddr, int fVal, Redcode.AddressingMode mode) {
        switch (mode) {
            case DIRECT:
                return getCode(curAddr+fVal).getFields();
            case IMMEDIATE:
                return new int[] {0, fVal};
            case A_INDIRECT:
                return getFieldsWithAddrMode(curAddr+fVal, getCode(curAddr+fVal).aField, Redcode.AddressingMode.DIRECT);
            case B_INDIRECT:
                return getFieldsWithAddrMode(curAddr+fVal, getCode(curAddr+fVal).bField, Redcode.AddressingMode.DIRECT);
        }
        throw new IllegalArgumentException("invalid mode " + mode);
    }

    private Redcode getCodeWithAddrMode(int curAddr, int fVal, Redcode.AddressingMode mode) {
        switch (mode) {
            case DIRECT:
                return getCode(curAddr+fVal);
            case IMMEDIATE:
                return getCode(curAddr);
            case A_INDIRECT:
                return getCodeWithAddrMode(curAddr+fVal, getCode(curAddr+fVal).aField, Redcode.AddressingMode.DIRECT);
            case B_INDIRECT:
                return getCodeWithAddrMode(curAddr+fVal, getCode(curAddr+fVal).bField, Redcode.AddressingMode.DIRECT);
        }
        throw new IllegalArgumentException("invalid mode " + mode);

    }

    private int getLocWithAddrMode(int curAddr, int fVal, Redcode.AddressingMode mode) {
        switch (mode) {
            case DIRECT:
                return curAddr+fVal;
            case IMMEDIATE:
                return curAddr;
            case A_INDIRECT:
                return curAddr+fVal + getCode(curAddr+fVal).aField;
            case B_INDIRECT:
                return curAddr+fVal + getCode(curAddr+fVal).bField;
        }
        throw new IllegalArgumentException("invalid mode " + mode);
    }

    public static void main(String[] args) throws Exception {
        Simulator sim = new Simulator(200);
        List<Redcode> imp = new ArrayList<>();
        imp.add(new Redcode(Redcode.Operator.MOV, 0, Redcode.AddressingMode.DIRECT, 1, Redcode.AddressingMode.DIRECT, 0, 0));

        List<Redcode> dwarf = new ArrayList<>();
        dwarf.add(new Redcode(Redcode.Operator.ADD, 4, Redcode.AddressingMode.IMMEDIATE, 3, Redcode.AddressingMode.DIRECT, 1, 0));
        dwarf.add(new Redcode(Redcode.Operator.MOV, 2, Redcode.AddressingMode.DIRECT, 2, Redcode.AddressingMode.B_INDIRECT, 1, 0));
        dwarf.add(new Redcode(Redcode.Operator.JMP, -2, Redcode.AddressingMode.DIRECT, 0, null, 1, 0));
        dwarf.add(new Redcode(Redcode.Operator.DATA, 0, Redcode.AddressingMode.IMMEDIATE, 0, Redcode.AddressingMode.IMMEDIATE, 1, 0));

        sim.spawn(0, 0, imp);
        sim.spawn(1, 0, dwarf);

        sim.start();

        sim.printBuffer();
        System.out.println();
        Thread.sleep(800);
        while (sim.step()) {
            sim.printBuffer();
            System.out.println();
            Thread.sleep(800);
        }
    }

    private void printBuffer() {
        int i = 0;
        while (i < buffer.length) {
            for (int j = 0; j < DISPLAY_WIDTH; j++) {
                int pid = buffer[i].programID;
                if (pid >= 0) {
                    System.out.print(COLORS[pid%COLORS.length] + " " + ANSI_RESET);
                    System.out.print(" ");
                } else {
                    System.out.print("X ");
                }
                i++;
            }
            System.out.println();
        }
    }
}
