package Units;

import CPU.CPU;
import Core.Register;
import Instructions.ControlTransferInstruction;
import Instructions.Instruction.OPCODE;

public class ICU {
    private interface Command {
        public boolean execute(String label);
    }

    private class JmpCommand implements Command {
        public boolean execute(String label) {
            CPU.writeRegister(Register.OPERAND.GH, CPU.readLabel(label));
            return true;
        }
    }

    private class JlCommand implements Command {
        public boolean execute(String label) {
            boolean satisfied = false;
            if (CPU.readRegister(Register.OPERAND.DA) == (byte)-1) {
                CPU.writeRegister(Register.OPERAND.GH, CPU.readLabel(label));
                satisfied = true;
            }
            return satisfied;
        }
    }

    private class JgCommand implements Command {
        public boolean execute(String label) {
            boolean satisfied = false;
            if (CPU.readRegister(Register.OPERAND.DA) == (byte)1) {
                CPU.writeRegister(Register.OPERAND.GH, CPU.readLabel(label));
                satisfied = true;
            }
            return satisfied;
        }
    }

    private class JeCommand implements Command {
        public boolean execute(String label) {
            boolean satisfied = false;
            if (CPU.readRegister(Register.OPERAND.DA) == (byte)0) {
                CPU.writeRegister(Register.OPERAND.GH, CPU.readLabel(label));
                satisfied = true;
            }
            return satisfied;
        }
    }

    private void callCommand(Command command, String label) {
        if (!command.execute(label)) {
            CPU.advanceEip(1);
        }
    }

    public void execute(ControlTransferInstruction instr) {
        Command command = null;
        if (instr.opcode() == OPCODE.JMP) {
            command = new JmpCommand();
        } else if (instr.opcode() == OPCODE.JG) {
            command = new JgCommand();
        } else if (instr.opcode() == OPCODE.JL) {
            command = new JlCommand();
        } else if (instr.opcode() == OPCODE.JE) {
            command = new JeCommand();
        } else {
            System.err.println("opcode not found");
            System.exit(1);
        }
    
        callCommand(command, CPU.getLabelName(instr.label()));
    }
}
