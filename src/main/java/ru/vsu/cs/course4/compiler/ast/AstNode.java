package ru.vsu.cs.course4.compiler.ast;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface AstNode {

    default Collection<? extends AstNode> childs() {
        return null;
    }

    default Collection<String> tree() {
        List<String> res = new LinkedList<>();
        res.add(toString());
        Collection<? extends AstNode> childs = childs();
        if (childs != null) {
            int i = 0;
            for (AstNode child : childs) {
                String ch0 = "├", ch = "│";
                if (i++ == childs.size() - 1) {
                    ch0 = "└"; ch = " ";
                }
                Collection<String> tree = child.tree();
                if (tree == null) {
                    continue;
                }
                int j = 0;
                for (String s : tree) {
                    res.add((j++ == 0 ? ch0 : ch) + ' ' + s);
                }
            }
        }
        return res;
    }

    default void printTree(PrintStream printStream) {
        Collection<String> tree = tree();
        if (tree == null) {
            return;
        }
        for (String s : tree) {
            printStream.println(s);
        }
    }


    static AstNode group(String strInTree, Collection<? extends AstNode> childs) {
        return new AstNode() {
            String strInTreeField = strInTree;
            Collection<? extends AstNode> childsField = childs;

            @Override
            public Collection<? extends AstNode> childs() {
                return childsField;
            }

            @Override
            public String toString() {
                return strInTreeField;
            }
        };
    }
}
