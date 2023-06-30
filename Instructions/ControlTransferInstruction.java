package Instructions;

public class ControlTransferInstruction extends Instruction {
    public ControlTransferInstruction(OPCODE opcode, byte label) {
        this._opcode = opcode;
        this._label = label;
    }

    public byte label() {
        return _label;
    }

    private byte _label;
}
