package org.query.calc;

import java.io.IOException;
import java.util.Arrays;

public final class BCDataset {
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

    private void cacheSumYzProducts() {
        double sumYzProduct = 0;
        BCRecord previousRecord = null;
        int sameBPlusCCount = 0;
        for (int i = records.length - 1; i >= 0; i--) {
            BCRecord record = records[i];
            double previousSumYzProduct = sumYzProduct;
            sumYzProduct += record.getYZProduct();
            if (previousRecord == null || previousRecord.getBPlusC() != record.getBPlusC()) {
                record.setSumYzProduct(sumYzProduct);
                setSumYzProductToPreviousRecords(sameBPlusCCount, i + 1, previousSumYzProduct);
                sameBPlusCCount = 1;
            } else {
                sameBPlusCCount += 1;
            }
            previousRecord = record;
        }
        setSumYzProductToPreviousRecords(sameBPlusCCount, 0, sumYzProduct);
    }

    private void setSumYzProductToPreviousRecords(int sameBPlusCCount, int i, double previousSumYzProduct) {
        if (sameBPlusCCount > 1) {
            // setting the correct product to the previous records
            for (int j = i; j < i + sameBPlusCCount; j++) {
                records[j].setSumYzProduct(previousSumYzProduct);
            }
        }
    }
}
