package Instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Pattern;

import Constants.Constants;
import CPU.*;
import Core.Register.OPERAND;
import Instructions.Instruction.*;
import Utils.BitsManipulation;

public class InstructionParser {
    public static SimpleEntry<COMMAND_TYPE, BitSet>
    instructionToBinary(String instructionStr) {
        instructionStr = instructionStr.strip();
    
        ArrayList<String> instructionArr = new ArrayList<String>(Arrays.asList(instructionStr.split("\\s+")));
        instructionArr.remove(",");

        String commandName = instructionArr.get(0);
        OPCODE opcode = OPCODE.valueOf(commandName);

        return convertToBinary(opcode, instructionArr.subList(1, instructionArr.size()));
    }
    
    private static SimpleEntry<COMMAND_TYPE, BitSet>
    convertToBinary(OPCODE opcode, List<String> args) {
        if (args.isEmpty()) {
            return null;
        }
    
        SimpleEntry<COMMAND_TYPE, BitSet> res =
            new SimpleEntry<COMMAND_TYPE, BitSet>(Instruction.commandType(opcode), null);

        if (args.get(0).charAt(args.get(0).length() - 1) == ',') {
            args.set(0, args.get(0).substring(0, args.get(0).length() - 1));
        }

        switch (res.getKey()) {
            case ARITHMETICAL:
            case LOGICAL:
                res.setValue(convertALUInstruction(opcode, args));
                break;
            case CONTROL_TRANSFER:
                res.setValue(convertControlTransferInstruction(opcode, args));
                break;
            case DATA_TRANSFER:
                res.setValue(convertDataTransferInstruction(opcode, args));
                break;
            default:
                System.err.println("Unknown Command type");
                System.exit(1);
        }

        return res;
    }

    private static OPERAND getRegCode(String registerName) {
        OPERAND dest = null;
        try {
            dest = OPERAND.valueOf(registerName);
        } catch (IllegalArgumentException e) { }

        return dest;
    }

    private static BitSet memoryDataTransferToBinary(byte dest, byte src,
                                                               boolean destIsReg, boolean srcIsReg) {
        BitSet binaryInstruction = new BitSet(Constants.DT_INSTR_BITS_LEN);

        int opcodeStartIx = Constants.DT_INSTR_BITS_LEN - Constants.OPCODE_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          OPCODE._MEMORY_TRANSFER.getValue(),
                          opcodeStartIx,
                          Constants.OPCODE_BITS_LEN);

        int destStartIx = opcodeStartIx - _FAKE_ADDRESS_BITS_COUNT;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          dest,
                          destStartIx,
                          _FAKE_ADDRESS_BITS_COUNT);

        int srcStartIx = destStartIx - _FAKE_ADDRESS_BITS_COUNT;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          src, srcStartIx,
                          _FAKE_ADDRESS_BITS_COUNT);

        binaryInstruction.set(1, destIsReg);
        binaryInstruction.set(0, srcIsReg);

        return binaryInstruction;
    }

    private static BitSet convertDataTransferInstruction(OPCODE opcode, List<String> args) {
        if (args.size() != 2) {
            System.err.println("invalid args count for data transfer command");
            System.exit(1);
        }

        String destination = args.get(0);
        OPERAND destinationRegisterCode = null;
        byte destinationMemoryAddress = -1;

        if (Pattern.matches(_MemoryAddressRegex, destination)) {
            destination = destination.substring(1, args.get(0).length() - 1);
            destinationMemoryAddress = Byte.parseByte(destination);
        } else {
            destinationRegisterCode = getRegCode(destination);
            if (destinationRegisterCode == null) {
                System.err.println("Register not found");
                System.exit(1);
            }
        }

        String source = args.get(1);
        byte srcValue = 0;
        byte sourceMemoryAddress = -1;
        boolean sourceIsRegister = false;

        if (Pattern.matches(_MemoryAddressRegex, source)) {
            source = source.substring(1, args.get(0).length() - 1);
            sourceMemoryAddress = Byte.parseByte(source);
        } else {
            try {
                srcValue = Byte.parseByte(source);
            } catch (NumberFormatException e) {
                OPERAND sourceRegisterCode = getRegCode(source);
                if (sourceRegisterCode == null) {
                    System.err.println("Register not found");
                    System.exit(1);
                }
                srcValue = sourceRegisterCode.getValue();
                sourceIsRegister = true;
            }
        }

        BitSet binaryInstruction = new BitSet(Constants.DT_INSTR_BITS_LEN);
        if (destinationMemoryAddress != -1 || sourceMemoryAddress != -1) {
            if (sourceMemoryAddress == -1 && !sourceIsRegister) {
                System.err.println("source cannot be a literal value when working with memory addresses");
                System.exit(1);
            }

            boolean destinationIsAddress = (destinationRegisterCode == null);
            byte destinationValue = destinationMemoryAddress;
            if (!destinationIsAddress) {
                destinationValue = destinationRegisterCode.getValue();
            }

            binaryInstruction = memoryDataTransferToBinary(destinationValue,
                                                           srcValue,
                                                           !destinationIsAddress,
                                                           sourceIsRegister);
        } else {
            binaryInstruction =
                simpleDataTransferToBinary(destinationRegisterCode, srcValue, sourceIsRegister);
        }

        return binaryInstruction;
    }

    private static BitSet simpleDataTransferToBinary(OPERAND destinationRegisterCode,
                                                     byte srcValue,
                                                     boolean sourceIsRegister) {
        BitSet binaryInstruction = new BitSet(Constants.DT_INSTR_BITS_LEN);

        int opcodeStartIx = Constants.DT_INSTR_BITS_LEN - Constants.OPCODE_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          OPCODE.MOV.getValue(),
                          opcodeStartIx,
                          Constants.OPCODE_BITS_LEN);

        int operandStartIx = opcodeStartIx - Constants.OPERAND_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          destinationRegisterCode.getValue(),
                          operandStartIx,
                          Constants.OPERAND_BITS_LEN);    

        int sourceStartIx = operandStartIx - Byte.SIZE;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          srcValue,
                          sourceStartIx,
                          Byte.SIZE);

        binaryInstruction.set(0, sourceIsRegister);

        return binaryInstruction;
    }

    private static BitSet convertControlTransferInstruction(OPCODE opcode, List<String> args) {
        if (args.size() != 1) {
            System.err.println("invalid args count for control transfer command");
            System.exit(1);
        }
        BitSet binaryInstruction = new BitSet(Constants.CT_INSTR_BITS_LEN);
        
        int opcodeStartIx = Constants.CT_INSTR_BITS_LEN - Constants.OPCODE_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction, opcode.getValue(), opcodeStartIx, Constants.OPCODE_BITS_LEN);
        
        byte labelIx = CPU.readLabel(args.get(0));
        BitsManipulation.setBitsetFromByte(binaryInstruction, labelIx, 0, Constants.LABEL_INDEX_BITS_LEN);

        return binaryInstruction;
    }

    private static BitSet convertALUInstruction(OPCODE opcode, List<String> args) {
        OPERAND destinationRegisterCode = getRegCode(args.get(0));
        if (destinationRegisterCode == null) {
            System.err.println("Register not found");
            System.exit(1);
        }

        byte srcValue = 0;
        boolean sourceIsRegister = false;

        if (args.size() == 1) {
            if (opcode != OPCODE.NOT) {
                System.err.println(String.format("Invalid argument count for command: %s", opcode.name()));
                System.exit(1);
            }
            srcValue = (byte)0b11111111;
        } else if (args.size() == 2) {
            try {
                srcValue = Byte.parseByte(args.get(1));
            } catch (NumberFormatException e) {
                srcValue = getRegCode(args.get(1)).getValue();
                if (destinationRegisterCode == null) {
                    System.err.println("Register not found");
                    System.exit(1);
                }
                sourceIsRegister = true;
            }
        } else {
            System.err.println("Too many arguments for the command");
            System.exit(1);
        }

        BitSet binaryInstruction = new BitSet(Constants.AL_INSTR_BITS_LEN);

        int opcodeStartIx = Constants.AL_INSTR_BITS_LEN - Constants.OPCODE_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          opcode.getValue(),
                          opcodeStartIx,
                          Constants.OPCODE_BITS_LEN);

        int operandStartIx = opcodeStartIx - Constants.OPERAND_BITS_LEN;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          destinationRegisterCode.getValue(),
                          operandStartIx,
                          Constants.OPERAND_BITS_LEN);    

        int sourceStartIx = operandStartIx - Byte.SIZE;
        BitsManipulation.setBitsetFromByte(binaryInstruction,
                          srcValue,
                          sourceStartIx,
                          Byte.SIZE);

        binaryInstruction.set(0, sourceIsRegister);

        return binaryInstruction;
    }

    private static final int _FAKE_ADDRESS_BITS_COUNT =
        (int)(Math.log((double)Constants.MEMORY_BYTES_LEN) / Math.log(2.0));

    private static final String _MemoryAddressRegex = "\\[(\\d+)\\]";
}
