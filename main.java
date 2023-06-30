import CPU.CPU;
import FakeMemory.FakeMemory;

class MainClass {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("\nWrong argument count.\nExpected only file path where instructions are stored.\n");
            System.exit(1);
        }

        CPU.execute(args[0]);
        FakeMemory.dump();
    }
}
