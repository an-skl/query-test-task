package org.query.calc;

import java.io.IOException;
import java.util.Arrays;

final class BCDataset {
    private final BCRecord[] records;

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
        cacheSumYzProducts();
    }

    public BCRecord[] getRecordsSortedByBPlusC() {
        return records;
    }

    public BCRecord findWithBPlusCGreaterThan(double exclMinBPlusC) {
        return findWithBPlusCGreaterThan(exclMinBPlusC, 0, records.length);

    }

    public BCRecord findWithBPlusCGreaterThan(double exclMinBPlusC, int fromIndex, int toIndex) {
        int bcIndex = findRecordIndexWithBPlusCGreaterThan(exclMinBPlusC, fromIndex, toIndex);
        if (bcIndex < records.length) {
            return records[bcIndex];
        }
        return null;
    }

    public int findRecordIndexWithBPlusCGreaterThan(double exclMinBPlusC, int fromIndex, int toIndex) {
        int bcIndex = Arrays.binarySearch(records,
            fromIndex,
            toIndex,
            new BCRecord(exclMinBPlusC, 0));
        if (bcIndex < 0) {
            bcIndex = - (bcIndex + 1);
        } else {
            while(bcIndex < records.length && records[bcIndex].getBPlusC() == exclMinBPlusC) {
                bcIndex++;
            }
        }
        return bcIndex;
    }

    private void cacheSumYzProducts() {
        double minSumYzProduct = Double.POSITIVE_INFINITY;
        double maxSumYzProduct = Double.NEGATIVE_INFINITY;
        double sumYzProduct = 0;
        BCRecord previousRecord = null;
        int sameBPlusCCount = 0;
        for (int i = records.length - 1; i >= 0; i--) {
            BCRecord record = records[i];
            double previousSumYzProduct = sumYzProduct;
            sumYzProduct += record.getYZProduct();

            double previousMinSumYzProduct = minSumYzProduct;
            minSumYzProduct = Math.min(sumYzProduct, minSumYzProduct);

            double previousMaxSumYzProduct = maxSumYzProduct;
            maxSumYzProduct = Math.max(sumYzProduct, maxSumYzProduct);

            if (previousRecord == null || previousRecord.getBPlusC() != record.getBPlusC()) {
                record.setSumYzProduct(sumYzProduct);
                record.setMinSumYzProduct(minSumYzProduct);
                record.setMaxSumYzProduct(maxSumYzProduct);
                setSumYzProductToPreviousRecords(sameBPlusCCount, i + 1, previousSumYzProduct, previousMinSumYzProduct, previousMaxSumYzProduct);
                sameBPlusCCount = 1;
            } else {
                sameBPlusCCount += 1;
            }
            previousRecord = record;
        }
        setSumYzProductToPreviousRecords(sameBPlusCCount, 0, sumYzProduct, minSumYzProduct, maxSumYzProduct);
    }

    private void setSumYzProductToPreviousRecords(int sameBPlusCCount, int i, double previousSumYzProduct,
                                                  double minSumYzProduct, double maxSumYzProduct) {
        if (sameBPlusCCount > 1) {
            // setting the correct product to the previous records
            for (int j = i; j < i + sameBPlusCCount; j++) {
                BCRecord previousRecord = records[j];
                previousRecord.setSumYzProduct(previousSumYzProduct);
                previousRecord.setMinSumYzProduct(minSumYzProduct);
                previousRecord.setMaxSumYzProduct(maxSumYzProduct);
            }
        }
    }
}
