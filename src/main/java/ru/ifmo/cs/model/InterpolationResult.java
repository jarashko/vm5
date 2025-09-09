package ru.ifmo.cs.model;

import java.util.List;

public class InterpolationResult {
    private double lagrangeValue;
    private double newtonDividedValue;
    private double newtonFiniteValue;
    private double stirlingValue;
    private double besselValue;
    private double[][] finiteDifferencesTable;
    private List<InterpolationPoint> points;
    private double targetX;
    
    public InterpolationResult(List<InterpolationPoint> points, double targetX) {
        this.points = points;
        this.targetX = targetX;
    }
    
    public double getLagrangeValue() {
        return lagrangeValue;
    }
    
    public void setLagrangeValue(double lagrangeValue) {
        this.lagrangeValue = lagrangeValue;
    }
    
    public double getNewtonDividedValue() {
        return newtonDividedValue;
    }
    
    public void setNewtonDividedValue(double newtonDividedValue) {
        this.newtonDividedValue = newtonDividedValue;
    }
    
    public double getNewtonFiniteValue() {
        return newtonFiniteValue;
    }
    
    public void setNewtonFiniteValue(double newtonFiniteValue) {
        this.newtonFiniteValue = newtonFiniteValue;
    }
    
    public double[][] getFiniteDifferencesTable() {
        return finiteDifferencesTable;
    }
    
    public void setFiniteDifferencesTable(double[][] finiteDifferencesTable) {
        this.finiteDifferencesTable = finiteDifferencesTable;
    }
    
    public List<InterpolationPoint> getPoints() {
        return points;
    }
    
    public double getTargetX() {
        return targetX;
    }
    
    public double getStirlingValue() {
        return stirlingValue;
    }
    
    public void setStirlingValue(double stirlingValue) {
        this.stirlingValue = stirlingValue;
    }
    
    public double getBesselValue() {
        return besselValue;
    }
    
    public void setBesselValue(double besselValue) {
        this.besselValue = besselValue;
    }
    
    public double getAverageDeviation() {
        double sum = Math.abs(lagrangeValue - newtonDividedValue) + 
                    Math.abs(lagrangeValue - newtonFiniteValue) + 
                    Math.abs(lagrangeValue - stirlingValue) +
                    Math.abs(lagrangeValue - besselValue) +
                    Math.abs(newtonDividedValue - newtonFiniteValue) +
                    Math.abs(newtonDividedValue - stirlingValue) +
                    Math.abs(newtonDividedValue - besselValue) +
                    Math.abs(newtonFiniteValue - stirlingValue) +
                    Math.abs(newtonFiniteValue - besselValue) +
                    Math.abs(stirlingValue - besselValue);
        return sum / 10.0;
    }
}
