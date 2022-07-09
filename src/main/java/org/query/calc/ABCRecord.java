package org.query.calc;

public final class ABCRecord {
    private final int rowNumber;
    private final double a;
    private double totalX;
    private double sumXyzProduct;

    public ABCRecord(int rowNumber, double a, double sumXyzProduct) {
        this.rowNumber = rowNumber;
        this.a = a;
        this.sumXyzProduct = sumXyzProduct;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public double getA() {
        return a;
    }

    public double getTotalX() {
        return totalX;
    }

    public void increaseTotalX(double x) {
        totalX += x;
    }

    public double getSumXyzProduct() {
        return sumXyzProduct;
    }

    public void setSumXyzProduct(double xyzProduct) {
        sumXyzProduct = xyzProduct;
    }
}
