package Constants;

public class Constants {
    public static final byte MAX_LABEL_COUNT = 8;

    public static final byte MEMORY_BYTES_LEN = 32;
    
    public static final int AL_INSTR_BITS_LEN = 2 * Byte.SIZE;

    public static final int CT_INSTR_BITS_LEN = Byte.SIZE;

    public static final int DT_INSTR_BITS_LEN = 2 * Byte.SIZE;

    public static final byte OPCODE_BITS_LEN = 4;

    public static final byte OPERAND_BITS_LEN = 3;

    public static final int LABEL_INDEX_BITS_LEN = 4;
}
