package ru.vsu.cs.course4.compiler.semantic;

public class SemanticError {
    private final String message;
    private final int line;
    private final int column;

    public SemanticError(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public SemanticError(String message) {
        this(message, -1, -1);
    }

    public String getMessage() { return message; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        if (line > 0) {
            return String.format("Semantic error at line %d, col %d: %s", line, column, message);
        }
        return "Semantic error: " + message;
    }
}
