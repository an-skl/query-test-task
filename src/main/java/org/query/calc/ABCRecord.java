package org.query.calc;

final class ABCRecord {
    private final int rowNumber;
    private final double a;
    private double totalX;
    private double sumXyzProduct;

    // max total x for this record and those with the higher a
    private double maxTotalX;

    // min total x for this record and those with the higher a
    private double minTotalX;

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

    public double getMaxTotalX() {
        return maxTotalX;
    }

    public void setMaxTotalX(double maxTotalX) {
        this.maxTotalX = maxTotalX;
    }

    public double getMinTotalX() {
        return minTotalX;
    }

    public void setMinTotalX(double minTotalX) {
        this.minTotalX = minTotalX;
    }
}
