package org.query.calc;

public final class ABCRecord {
    private final int rowNumber;
    private final double a;
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

    public double getSumXyzProduct() {
        return sumXyzProduct;
    }

    public void increaseSumXyzProduct(double xyzProduct) {
        sumXyzProduct += xyzProduct;
    }
}
