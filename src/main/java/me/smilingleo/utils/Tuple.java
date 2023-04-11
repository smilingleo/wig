package me.smilingleo.utils;

public class Tuple<T> {

    private String left;
    private T right;

    public Tuple(String left, T right) {
        this.left = left;
        this.right = right;
    }

    public static <T> Tuple tuple(String left, T right) {
        return new Tuple(left, right);
    }

    public String getLeft() {
        return left;
    }

    public T getRight() {
        return right;
    }
}
