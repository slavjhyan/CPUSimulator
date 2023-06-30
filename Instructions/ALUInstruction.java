package Instructions;

import Core.Register.OPERAND;;
public class ALUInstruction extends Instruction {
    public ALUInstruction(OPCODE opcode, OPERAND operand, byte src) {
        this._opcode = opcode;
        this._operand = operand;
        this._src = src;
    }

    public OPERAND destination() {
        return _operand;
    }

    public byte srcValue() {
        return _src;
    }

    private OPERAND _operand;

    private byte _src;
}
