package Instructions;

import Core.Register.OPERAND;
public class DataTransferInstruction extends Instruction {
    public DataTransferInstruction(OPERAND operand, byte src) {
        this._opcode = OPCODE.MOV;
        this._operand = operand;
        this._src = src;
        this._memoryAddress = -1;
    }

    public DataTransferInstruction(byte memoryAddress, byte src) {
        this._opcode = OPCODE._MEMORY_TRANSFER;
        this._memoryAddress = memoryAddress;
        this._src = src;
        this._operand = null;
    }

    public boolean destIsRegister() {
        return this._operand != null;
    }

    public byte destinationAddress() {
        return _memoryAddress;
    }

    public OPERAND destinationReg() {
        return _operand;
    }

    public byte srcValue() {
        return _src;
    }

    private OPERAND _operand;

    private byte _memoryAddress;

    private byte _src;
}
