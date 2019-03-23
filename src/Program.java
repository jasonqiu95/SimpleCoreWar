import java.util.ArrayList;
import java.util.List;

public class Program {
    int programID;
    List<Redcode> code;
    List<Integer> processes;
    int curProcessIdx;
    boolean alive;

    public Program(int programID, List<Redcode> code, int spawnLoc) {
        this.programID = programID;
        this.processes = new ArrayList<>();
        processes.add(spawnLoc);
        this.curProcessIdx = 0;
        this.code = new ArrayList<>();
        for (int i = 0; i < code.size(); i++) {
            this.code.add(new Redcode(code.get(i)));
        }
        alive = true;
    }

    public int nextProcessPointer() {
        int nextProcessPointer = processes.get(curProcessIdx);
        // increment current process pointer
        processes.set(curProcessIdx, nextProcessPointer+1);
        // go on to next process
        curProcessIdx = (curProcessIdx+1) % processes.size();
        return nextProcessPointer;
    }

    public void jumpCurProcess(int newLoc) {
        processes.set(curProcessIdx, newLoc);
    }

    public void kill() {
        alive = false;
    }
}
