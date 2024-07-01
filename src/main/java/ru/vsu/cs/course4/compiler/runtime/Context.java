package ru.vsu.cs.course4.compiler.runtime;

import java.util.HashMap;
import java.util.Map;

public class Context {
    Context parent;
    public Map<String, Value> values = new HashMap<>();


    public Context(Context parent) {
        this.parent = parent;
    }

    public void put(String name, Value value) {
        values.put(name, value);
    }

    public Value get(String name) {
        for (Context curr = this; curr != null; curr = curr.parent) {
            if (curr.values.containsKey(name)) {
                return curr.values.get(name);
            }
        }
        return null;
    }
}
