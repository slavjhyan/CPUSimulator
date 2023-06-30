package CPU;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;

import Constants.Constants;
import Core.Register;
import Core.Register.OPERAND;
import FakeMemory.FakeMemory;
import Instructions.Instruction;
import Instructions.ALUInstruction;
import Instructions.ControlTransferInstruction;
import Instructions.DataTransferInstruction;
import Instructions.InstructionParser;
import Instructions.Instruction.COMMAND_TYPE;
import Instructions.Instruction.OPCODE;
import Utils.BitsManipulation;
import Units.ALU;
import Units.DTU;
import Units.ICU;

public class CPU {
    public static void execute(String path) {
        createRegisters();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateMachineCode(lines);
        loadAndExecute();
    }

    public static void advanceEip(int count) {
        writeRegister(OPERAND.GH, (byte)(readRegister(OPERAND.GH) + count));
    }

    public static byte readRegister(Register.OPERAND code) {
        return _registers[code.getValue()].read();
    }

    public static void writeRegister(Register.OPERAND code, byte val) {
        _registers[code.getValue()].write(val);
    }

    private static void generateMachineCode(List<String> lines) {
        lines.removeIf(String::isEmpty);
        for (String line : lines) {
            if (isLabel(line)) {
                String labelName = line.substring(0, line.length() - 1);
                if (!validateLabelName(labelName)) {
                    System.exit(1);
                }
                addLabel(labelName, FakeMemory.memoryPointer());
                continue;
            }
            SimpleEntry<COMMAND_TYPE, BitSet> res = InstructionParser.instructionToBinary(line);
            FakeMemory.pushInstruction(res.getValue(), res.getKey());
        }
    }

    private static void loadAndExecute() {
        Register eip = _registers[Register.OPERAND.GH.ordinal()];
        eip.write((byte)0);
        while (eip.read() < FakeMemory.memoryPointer()) {
            Instruction currInstr = readInstruction(eip.read());
            executeInstruction(currInstr);
        }
    }

    private static void executeInstruction(Instruction instr) {
        COMMAND_TYPE commandType = Instruction.commandType(instr.opcode());
        switch (commandType) {
            case ARITHMETICAL:
            case LOGICAL:
                _alu.execute((ALUInstruction)instr);
                break;
            case CONTROL_TRANSFER:
                _icu.execute((ControlTransferInstruction)instr);
                break;
            case DATA_TRANSFER:
                _dtu.execute((DataTransferInstruction)instr);
                break;
            default:
                System.err.println("Unknown Command type");
                System.exit(1);
        }

        if (commandType != COMMAND_TYPE.CONTROL_TRANSFER) {
            advanceEip(2);
        }
    }

    private static Instruction readInstruction(byte instructionAddress) {
        byte currAddressVal = FakeMemory.read(instructionAddress);
        OPCODE opcode = opcodeFromByte(currAddressVal);
    
        Instruction res = null;
        COMMAND_TYPE cType = Instruction.commandType(opcode);
        switch (cType) {
            case ARITHMETICAL:
            case LOGICAL:
                res = readALUInstructionFrom(opcode, currAddressVal);
                break;
            case CONTROL_TRANSFER:
                res = readControlTransferInstructionFrom(opcode, currAddressVal);
                break;
            case DATA_TRANSFER:
                res = readDataTransferInstructionFrom(opcode, currAddressVal);
                break;
            default:
                System.err.println("Unknown Command type");
                System.exit(1);
        }

        return res;
    }

    private static Instruction readDataTransferInstructionFrom(OPCODE opcode, byte addressVal) {
        byte nextAddressVal = FakeMemory.read((byte)(CPU.readRegister(OPERAND.GH) + 1));
        BitSet binaryInstruction = BitsManipulation.bitsetFromBytes(new byte[] {addressVal, nextAddressVal});

        OPERAND destReg = null;
        byte srcValue = -1;

        Instruction res = null;
        if (opcode == OPCODE.MOV) {
            destReg = extractDestinationRegister(binaryInstruction);
            srcValue = extractSourceValue(binaryInstruction);
            boolean sourceIsReg = checkSourceIsRegister(binaryInstruction);

            if (sourceIsReg) {
                srcValue = CPU.readRegister(OPERAND.values()[srcValue]);
            }
            res = new DataTransferInstruction(destReg, srcValue);
        } else if (opcode == OPCODE._MEMORY_TRANSFER) {
            boolean transferFromMemory = !binaryInstruction.get(0);
            boolean transferToMemory = !binaryInstruction.get(1);
            
            byte srcLiteral = (byte)BitsManipulation.intFromBitSet(binaryInstruction, 2, 7);
            if (transferFromMemory) {
                srcValue = FakeMemory.read(srcLiteral);
            } else {
                srcValue = CPU.readRegister(OPERAND.values()[srcLiteral]);
            }

            byte destLiteral = (byte)BitsManipulation.intFromBitSet(binaryInstruction, 7, 12);

            if (transferToMemory) {
                res = new DataTransferInstruction(destLiteral, srcValue);
            } else {
                destReg = OPERAND.values()[destLiteral];
                res = new DataTransferInstruction(destReg, srcValue);
            }
        } else {
            System.err.println("Unknown data transfer instruction");
            System.exit(1);
        }

        return res;
    }

    private static Instruction readControlTransferInstructionFrom(OPCODE opcode, byte addressVal) {
        BitSet binaryInstruction = BitsManipulation.bitsetFromBytes(new byte[] {addressVal});
    
        byte labelCode =
            (byte)BitsManipulation.intFromBitSet(binaryInstruction, 0,
                                Constants.CT_INSTR_BITS_LEN - Constants.LABEL_INDEX_BITS_LEN);

        return new ControlTransferInstruction(opcode, labelCode);
    }

    private static Instruction readALUInstructionFrom(OPCODE opcode, byte addressVal) {
        byte nextAddressVal = FakeMemory.read((byte)(CPU.readRegister(OPERAND.GH) + 1));
        BitSet binaryInstruction = BitsManipulation.bitsetFromBytes(new byte[] {addressVal, nextAddressVal});
    
        OPERAND dest = extractDestinationRegister(binaryInstruction);
        byte srcValue = extractSourceValue(binaryInstruction);
        boolean sourceIsReg = checkSourceIsRegister(binaryInstruction);
        if (sourceIsReg) {
            srcValue = CPU.readRegister(Register.OPERAND.values()[srcValue]);
        }

        return new ALUInstruction(opcode, dest, srcValue);
    }

    private static OPERAND extractDestinationRegister(BitSet binaryInstruction) {
        int endIx = 2 * Byte.SIZE - Constants.OPCODE_BITS_LEN;
        int startIx = endIx - Constants.OPERAND_BITS_LEN;
        int registerCode = BitsManipulation.intFromBitSet(binaryInstruction, startIx, endIx);

        return OPERAND.values()[registerCode];
    }

    private static byte extractSourceValue(BitSet binaryInstruction) {
        int endIx = 2 * Byte.SIZE - Constants.OPCODE_BITS_LEN - Constants.OPERAND_BITS_LEN;
        int startIx = endIx - Byte.SIZE;
        int val = BitsManipulation.intFromBitSet(binaryInstruction, startIx, endIx);

        return (byte)val;
    }

    private static boolean checkSourceIsRegister(BitSet binaryInstruction) {
        return binaryInstruction.get(0);
    }

    private static OPCODE opcodeFromByte(byte addressVal) {
        byte code = 0;
        for (int i = 0; i < Constants.OPCODE_BITS_LEN; ++i) {
            byte bit = (byte)(BitsManipulation.getNthBit((int)addressVal, i + Constants.LABEL_INDEX_BITS_LEN) ? 1 : 0);

            code |= bit << i;
        }
        return OPCODE.values()[code];
    }

    public static enum ERROR_CODE {
        OVERFLOW(1);
    
        ERROR_CODE(int code) {
            this._code = (byte)code;
        }

        public byte getCode() {
            return _code;
        }

        private byte _code;
    }

    private static void addLabel(String name, byte address) {
        if (_labelCount > Constants.MAX_LABEL_COUNT - 1) {
            System.err.println("Exceeded labels amount");
            System.exit(1);
        }
        _labels.set(_labelCount++, new SimpleEntry<>(name, Byte.valueOf(address)));
    }
    
    public static byte readLabel(String name) {
        byte address = -1;
        for (int i = 0; i < _labelCount; ++i) {
            if (_labels.get(i).getKey().equals(name)) {
                address = _labels.get(i).getValue();
                break;
            }
        }
        return address;
    }

    public static String getLabelName(byte labelVal) {
        String name = "";
        for (int i = 0; i < _labelCount; ++i) {
            if (_labels.get(i).getValue() == labelVal) {
                name = _labels.get(i).getKey();
                break;
            }
        }
        return name;
    }

    private static boolean isLabel(String line) {
        if (line.charAt(line.length() - 1) == ':') {
            return true;
        }
        return false;
    }

    private static boolean validateLabelName(String newLabelName) {
        for (int i = 0; i < newLabelName.length(); ++i) {
            char currChar = newLabelName.charAt(i);
            if (!((currChar >= 'a' && currChar <= 'z') ||
                (currChar >= '0' && currChar <= '9'))) {
                System.err.println(
                    String.format("Invalid label name: '%s'\nShould contain only lowercase letters and/or digits",
                                 newLabelName));
                return false;
            }
        }
    
        for (int i = 0; i < _labelCount; ++i) {
            if (_labels.get(i).getKey() == newLabelName) {
                System.err.println(String.format("Label '%s' already exists", newLabelName));
                return false;
            }
        }

        return true;
    }

    private static Register[] _registers = new Register[Register.OPERAND.values().length];

    private static void createRegisters() {
        for (int i = 0; i < Register.OPERAND.values().length; ++i) {
            _registers[i] = Register.create(Register.OPERAND.values()[i]);
        }
    }

    private static ALU _alu = new ALU();

    private static DTU _dtu = new DTU();

    private static ICU _icu = new ICU();

    private static byte _labelCount = 0;

    private static ArrayList<SimpleEntry<String, Byte>> _labels =
        new ArrayList<>(Collections.nCopies(Constants.MAX_LABEL_COUNT, new SimpleEntry<>("", (byte) 0)));
}
