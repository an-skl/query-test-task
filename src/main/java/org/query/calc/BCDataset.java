package org.query.calc;

import java.io.IOException;
import java.util.Arrays;

public final class BCDataset {
    private final BCRecord[] records;
    private int cachedProductsIndex;
    private double cachedSumYzProduct = 0.0;

    public BCDataset(TuplesFileReader bReader, TuplesFileReader cReader) throws IOException {
        double[] rawCRecords = new double[cReader.getCount() * 2];
        int cPosition = 0;
        while (cReader.hasNext()) {
            DoubleTuple cRecord = cReader.next();
            rawCRecords[cPosition * 2] = cRecord.getV1();
            rawCRecords[cPosition * 2 + 1] = cRecord.getV2();
            cPosition += 1;
        }

        records = new BCRecord[bReader.getCount() * cReader.getCount()];
        cachedProductsIndex = records.length;
        int position = 0;
        while (bReader.hasNext()) {
            DoubleTuple bRecord = bReader.next();
            double b = bRecord.getV1();
            double y = bRecord.getV2();
            for (int i = 0; i < cReader.getCount(); i++) {
                double c = rawCRecords[i * 2];
                double z = rawCRecords[i * 2 + 1];
                records[position] = new BCRecord(b + c, y * z);
                position += 1;
            }
        }

        Arrays.sort(records);
    }

    public double calcSumYzProduct(double bPlusCGreaterThan) {
        int position = Arrays.binarySearch(
            records,
            new BCRecord(bPlusCGreaterThan, 0d),
            (o1, o2) -> Double.compare(o1.getBPlusC(), o2.getBPlusC()));
        if (position >= 0) {
            // exact match found, whereas the record needs to have b + c greater than the specified argument
            position += 1;
        } else {
            // exact match not found, the position to insert must have a value greater than sought one
            position = -position - 1;
        }
        if (position < records.length) {
            cacheSumYzProducts(position); // caching products for this b + c and above
            return records[position].getSumYzProduct();
        }
        return 0.0;
    }

    private void cacheSumYzProducts(int minIndex) {
        if (cachedProductsIndex <= minIndex) {
            return;
        }
        double soughtBPlusC = records[minIndex].getBPlusC();
        for (; minIndex > 0; minIndex--) {
            BCRecord previousRecord = records[minIndex - 1];
            if (previousRecord.getBPlusC() != soughtBPlusC) {
                break;
            }
        }

        double sumYzProduct = cachedSumYzProduct;
        BCRecord previousRecord = null;
        int sameBPlusCCount = 0;
        for (int i = cachedProductsIndex - 1; i >= minIndex; i--) {
            BCRecord record = records[i];
            double previousSumYzProduct = sumYzProduct;
            sumYzProduct += record.getYZProduct();
            if (previousRecord == null || previousRecord.getBPlusC() != record.getBPlusC()) {
                record.setSumYzProduct(sumYzProduct);
                setSumYzProductToPreviousRecords(sameBPlusCCount, i, previousSumYzProduct);
                sameBPlusCCount = 1;
            } else {
                sameBPlusCCount += 1;
            }
            previousRecord = record;
        }
        setSumYzProductToPreviousRecords(sameBPlusCCount, minIndex - 1, sumYzProduct);
        cachedSumYzProduct = sumYzProduct;
        cachedProductsIndex = minIndex;
    }

    private void setSumYzProductToPreviousRecords(int sameBPlusCCount, int i, double previousSumYzProduct) {
        if (sameBPlusCCount > 1) {
            // setting the correct product to the previous records
            for (int j = i + 1; j < i + sameBPlusCCount + 1; j++) {
                records[j].setSumYzProduct(previousSumYzProduct);
            }
        }
    }
}
