package org.query.calc;

public final class BCRecord implements Comparable<BCRecord> {
    private final double bPlusC;
    private final double yzProduct;
    private double sumYzProduct;

    public BCRecord(double bPlusC, double yzProduct) {
        this.bPlusC = bPlusC;
        this.yzProduct = yzProduct;
    }

    public double getBPlusC() {
        return bPlusC;
    }

    public double getYZProduct() {
        return yzProduct;
    }

    public double getSumYzProduct() {
        return sumYzProduct;
    }

    public void setSumYzProduct(double value) {
        sumYzProduct = value;
    }

    @Override
    public int compareTo(BCRecord another) {
        if (another == null) {
            return 1;
        }
        return Double.compare(this.bPlusC, another.bPlusC);
    }
}
