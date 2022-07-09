package org.query.calc;

final class BCRecord implements Comparable<BCRecord> {
    private final double bPlusC;
    private final double yzProduct;

    // SUM(Y * Z) for this record and those with higher b + c
    private double sumYzProduct;

    // max(SUM(Y * Z)) for this record and those with higher b + c
    private double maxSumYzProduct;

    // min(SUM(Y * Z)) for this record and those with higher b + c
    private double minSumYzProduct;

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

    public double getMaxSumYzProduct() {
        return maxSumYzProduct;
    }

    public void setMaxSumYzProduct(double maxSumYzProduct) {
        this.maxSumYzProduct = maxSumYzProduct;
    }

    public double getMinSumYzProduct() {
        return minSumYzProduct;
    }

    public void setMinSumYzProduct(double minSumYzProduct) {
        this.minSumYzProduct = minSumYzProduct;
    }

    @Override
    public int compareTo(BCRecord another) {
        if (another == null) {
            return 1;
        }
        return Double.compare(this.bPlusC, another.bPlusC);
    }
}
