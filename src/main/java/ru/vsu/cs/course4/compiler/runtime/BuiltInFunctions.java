package ru.vsu.cs.course4.compiler.runtime;

import java.lang.reflect.Method;
import java.util.*;

public class BuiltInFunctions {

    public static class ConvertException extends InterpreterException {
        public ConvertException(String message){
            super(message);
        }
    }

    public static Map<String, Method> FUNCTIONS = new HashMap<>();

    static {
        for (Method m : BuiltInFunctions.class.getDeclaredMethods()) {
            String name = m.getName();
            if (name.startsWith("$")) {
                name = name.substring(1);
            }
            FUNCTIONS.put(name, m);
        }
    }

    public static void println(Value v) {
        System.out.println(v.toString());
    }
    public static void print(Value v) {
        System.out.print(v.toString());
    }

    public static Value sin(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            return new Value(Math.sin(value.getInt()));
        } else if (value.type == Type.DOUBLE) {
            return new Value(Math.sin(value.getDouble()));
        } else {
            throw new InterpreterException("Inappropriate value type");
        }
    }

    public static Value cos(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            return new Value(Math.cos(value.getInt()));
        } else if (value.type == Type.DOUBLE) {
            return new Value(Math.cos(value.getDouble()));
        } else {
            throw new InterpreterException("Inappropriate value type");
        }
    }

    public static Value pow(Value base, Value exponent) throws InterpreterException {
        switch (base.type) {
            case INT:
                switch (exponent.type) {
                    case INT:
                        return new Value(Math.pow(base.getInt(), exponent.getInt()));
                    case DOUBLE:
                        return new Value(Math.pow(base.getInt(), exponent.getDouble()));
                    default:
                        throw new InterpreterException("Inappropriate exponent value type");
                }
            case DOUBLE:
                switch (exponent.type) {
                    case INT:
                        return new Value(Math.pow(base.getDouble(), exponent.getInt()));
                    case DOUBLE:
                        return new Value(Math.pow(base.getDouble(), exponent.getDouble()));
                    default:
                        throw new InterpreterException("Inappropriate exponent value type");
                }
            default:
                throw new InterpreterException("Inappropriate base value type");
        }
    }

    public static Value abs(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            return new Value(Math.abs(value.getInt()));
        } else if (value.type == Type.DOUBLE) {
            return new Value(Math.abs(value.getDouble()));
        } else {
            throw new InterpreterException("Inappropriate value type");
        }
    }

    public static Value sqrt(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            return new Value(Math.sqrt(value.getInt()));
        } else if (value.type == Type.DOUBLE) {
            return new Value(Math.sqrt(value.getDouble()));
        } else {
            throw new InterpreterException("Inappropriate value type");
        }
    }

    public static Value log(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            return new Value(Math.log(value.getInt()));
        } else if (value.type == Type.DOUBLE) {
            return new Value(Math.log(value.getDouble()));
        } else {
            throw new InterpreterException("Inappropriate value type");
        }
    }

    public static Value read() {
        Scanner scanner = new Scanner(System.in);
        return new Value(scanner.nextLine());
    }

    public static Value $int(Value value) throws ConvertException {
        if (value.type == Type.INT) {
            return value;
        } else if (value.type == Type.DOUBLE) {
            return new Value((int) value.getDouble());
        } else if (value.type == Type.BOOLEAN) {
            return new Value(value.getBool() ? 1 : 0);
        } else if (value.type == Type.STRING) {
            try {
                return new Value(Integer.parseInt(value.getStr()));
            } catch (Exception e) {
                throw new ConvertException(String.format("Can't convert \"%s\" to int", value.getStr()));
            }
        } else {
            throw new ConvertException(String.format("Can't convert \"%s\" to int", value.getStr()));
        }
    }

    public static Value $double(Value value) throws ConvertException {
        if (value.type == Type.DOUBLE) {
            return value;
        } else if (value.type == Type.INT) {
            return new Value((double) value.getInt());
        } else if (value.type == Type.BOOLEAN) {
            return new Value(value.getBool() ? 1.0 : 0.0);
        } else if (value.type == Type.STRING) {
            try {
                return new Value(Double.parseDouble(value.getStr()));
            } catch (Exception e) {
                throw new ConvertException(String.format("Can't convert \"%s\" to double", value.getStr()));
            }
        } else {
            throw new ConvertException(String.format("Can't convert \"%s\" to double", value.getStr()));
        }
    }

    public static Value $boolean(Value value) throws ConvertException {
        if (value.type == Type.BOOLEAN) {
            return value;
        } else if (value.type == Type.INT) {
            return new Value(value.getInt() != 0);
        } else if (value.type == Type.DOUBLE) {
            return new Value(value.getDouble() != 0.0);
        } else if (value.type == Type.STRING) {
            try {
                return new Value(Boolean.parseBoolean(value.getStr()));
            } catch (Exception e) {
                throw new ConvertException(String.format("Can't convert \"%s\" to boolean", value.getStr()));
            }
        } else {
            throw new ConvertException(String.format("Can't convert \"%s\" to boolean", value.getStr()));
        }
    }

    public static Value $string(Value value) {
        if (value.type == Type.STRING) {
            return value;
        } else {
            return new Value(String.valueOf(value));
        }
    }

    public static Value $size(Value value) throws InterpreterException {
        if (value.type == Type.ARR) {
            return new Value(value.getArr().size());
        } else {
            throw new InterpreterException("Value is not an array");
        }
    }

    public static Value newArray(Value value) throws InterpreterException {
        if (value.type == Type.INT) {
            List<Value> newArr = new ArrayList<>();
            for (int i = 0; i < value.getInt(); i++) {
                newArr.add(new Value(0));
            }
            return new Value(newArr);
        } else {
            throw new InterpreterException("Array size is not int");
        }
    }

    public static void addItem(Value arr, Value item) throws InterpreterException {
        if (arr.type == Type.ARR) {
            arr.getArr().add(item);
        } else {
            throw new InterpreterException("Is not array");
        }
    }
}
