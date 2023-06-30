package Instructions;

public abstract class Instruction {
    public static enum OPCODE {
        ADD(0b0000),
        SUB(0b0001),
        MUL(0b0010),
        DIV(0b0011),
        AND(0b0100),
        OR(0b0101),
        NOT(0b0110),
        CMP(0b0111),
        JMP(0b1000),
        JG(0b1001),
        JL(0b1010),
        JE(0b1011),
        MOV(0b1100),
        _MEMORY_TRANSFER(0b1101);

        private final byte value;

        OPCODE(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }

    }

    public static enum COMMAND_TYPE {
        ARITHMETICAL,
        LOGICAL,
        CONTROL_TRANSFER,
        DATA_TRANSFER
    };

    public static COMMAND_TYPE commandType(OPCODE opcode) {
        byte val = opcode.getValue();
        COMMAND_TYPE type = null;

        if (val < 4) {
            type = COMMAND_TYPE.ARITHMETICAL;
        } else if (val >= 4 && val < 8) {
            type = COMMAND_TYPE.LOGICAL;
        } else if (val >= 8 && val < 12) {
            type = COMMAND_TYPE.CONTROL_TRANSFER;
        } else if (val >= 12 && val < 14) {
            type = COMMAND_TYPE.DATA_TRANSFER;
        } else {
            System.err.println("Unknown command type");
            System.exit(1);
        }

        return type;
    }

    public static int instructionBitsLen(COMMAND_TYPE commandType) {
        int len = Byte.SIZE * 2;
        if (commandType == COMMAND_TYPE.CONTROL_TRANSFER) {
            len -= Byte.SIZE;
        }

        return len;
    }

    public OPCODE opcode() {
        return _opcode;
    }

    protected OPCODE _opcode;
}
