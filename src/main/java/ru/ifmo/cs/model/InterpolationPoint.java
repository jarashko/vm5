package ru.ifmo.cs.model;

public class InterpolationPoint {
    private double x;
    private double y;

    public InterpolationPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", x, y);
    }
}

