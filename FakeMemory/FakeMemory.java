package FakeMemory;

import java.util.BitSet;

import Constants.Constants;
import Instructions.Instruction;

public class FakeMemory {
    public static void dump() {
        for (byte i = 0; i < Constants.MEMORY_BYTES_LEN; ++i) {
            String hexAddress = String.format("%02X", i);
            String binValue = String.format("%8s", Integer.toBinaryString(_memory[i] & 0xFF)).replace(' ', '0');
            System.out.println(String.format("0x%s: 0b%s", hexAddress, binValue));
        }
    }

    public static void pushInstruction(BitSet instruction, Instruction.COMMAND_TYPE commandType) {
        int instrBytesLen = Instruction.instructionBitsLen(commandType) / 8;

        for (int i = instrBytesLen - 1; i >= 0; --i) {
            byte currByteVal = 0;
            for (int j = 0; j < Byte.SIZE; ++j) {
                if (instruction.get(j + Byte.SIZE * i)) {
                    currByteVal |= (1 << j);
                }
            }

            if (!pushByte(currByteVal)) {
                System.err.println("Memory Overflow");
            }
        }
    }

    public static void write(byte data, byte address) {
        if (address < _mp || address >= Constants.MEMORY_BYTES_LEN) {
            System.err.println(String.format("Access to address %s is forbidden", Byte.toString(address)));
            System.exit(1);
        }
        setMemory(address, data);
    }

    public static byte read(byte address) {
        return (address < Constants.MEMORY_BYTES_LEN) ? _memory[address] : null;
    }

    public static byte memoryPointer() {
        return _mp;
    }

    private static boolean pushByte(byte b) {
        boolean success = false;

        if (_mp < Constants.MEMORY_BYTES_LEN - 1) {
            _memory[_mp++] = b;
            success = true;
        }

        return success;
    }

    private static boolean setMemory(byte address, byte val) {
        boolean success = false;

        if (address < Constants.MEMORY_BYTES_LEN) {
            _memory[address] = val;
            success = true;
        }

        return success;
    }

    private static byte[] _memory = new byte[Constants.MEMORY_BYTES_LEN];

    private static byte _mp = 0;
}
