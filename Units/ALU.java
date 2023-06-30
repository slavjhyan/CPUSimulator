package Units;

import CPU.CPU;
import Core.Register;
import Instructions.ALUInstruction;
import Instructions.Instruction.OPCODE;

public class ALU {
    private interface Command {
        public void execute(Register.OPERAND dest, byte src);
    }

    private class AddCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) + src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class SubCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) - src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class MulCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) * src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class DivCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) / src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class AndCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) & src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class OrCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) | src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class NotCommand implements Command {
        public void execute(Register.OPERAND dest, byte src) {
            int res = (int)CPU.readRegister(dest) ^ src;
            if (res > Byte.MAX_VALUE || res < Byte.MIN_VALUE) {
                CPU.writeRegister(Register.OPERAND.ZA, CPU.ERROR_CODE.OVERFLOW.getCode());
            }
            CPU.writeRegister(dest, (byte)res);
        }
    }

    private class CmpCommand implements Command {
        public void execute(Register.OPERAND arg1, byte arg2Value) {
            byte arg1Value = CPU.readRegister(arg1);
            byte res = 0;
            if (arg1Value - arg2Value != 0) {
                res = (byte)((arg1Value - arg2Value) / Math.abs((arg1Value - arg2Value)));
            }
            CPU.writeRegister(Register.OPERAND.DA, res);
        }
    }

    private void callCommand(Command command, Register.OPERAND dest, byte src) {
        command.execute(dest, src);
    }

    public void execute(ALUInstruction instr) {
        Command command = null;
        if (instr.opcode() == OPCODE.ADD) {
            command = new AddCommand();
        } else if (instr.opcode() == OPCODE.SUB) {
            command = new SubCommand();
        } else if (instr.opcode() == OPCODE.MUL) {
            command = new MulCommand();
        } else if (instr.opcode() == OPCODE.DIV) {
            command = new DivCommand();
        } else if (instr.opcode() == OPCODE.AND) {
            command = new AndCommand();
        } else if (instr.opcode() == OPCODE.OR) {
            command = new OrCommand();
        } else if (instr.opcode() == OPCODE.NOT) {
            command = new NotCommand();
        } else if (instr.opcode() == OPCODE.CMP) {
            command = new CmpCommand();
        } else {
            System.err.println("opcode not found");
            System.exit(1);
        }
    
        callCommand(command, instr.destination(), instr.srcValue());
    }
}
