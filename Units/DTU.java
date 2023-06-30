package Units;

import CPU.CPU;
import FakeMemory.FakeMemory;
import Instructions.DataTransferInstruction;
import Instructions.Instruction.OPCODE;

public class DTU {

    public void execute(DataTransferInstruction instr) {
        if (instr.opcode() == OPCODE.MOV) {
            CPU.writeRegister(instr.destinationReg(), instr.srcValue());
        } else if (instr.opcode() == OPCODE._MEMORY_TRANSFER) {
            FakeMemory.write(instr.srcValue(), instr.destinationAddress());
        } else {
            System.err.println("opcode not found");
            System.exit(1);
        }
    }
}
