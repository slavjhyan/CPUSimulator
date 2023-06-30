package Core;

import java.util.ArrayList;

public class Register {
    public static enum OPERAND {
        AYB(0b000),
        BEN(0b001),
        GIM(0b010),
        DA(0b011),
        ECH(0b100),
        ZA(0b101),
        GH(0b110);

        private final byte value;

        OPERAND(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }
    }

    public static Register create(OPERAND code) {
        Register reg = null;
    
        boolean alreadyCreated = !_allRegOPERANDs.remove(code);
        if (!alreadyCreated) {
            reg = new Register(code);
        }
    
        return reg;
    }

    public void write(byte val) {
        this._value = val;
    }    

    public byte read() {
        return _value;
    }    

    public OPERAND code() {
        return _code;
    }    

    private Register(OPERAND code) {
        this._code = code;
    }    

    private byte _value;

    private OPERAND _code;

    private static ArrayList<OPERAND> _allRegOPERANDs = new ArrayList<OPERAND>();

    static {
        for (OPERAND regOPERAND : OPERAND.values()) {
            _allRegOPERANDs.add(regOPERAND);
        }
    }
}
