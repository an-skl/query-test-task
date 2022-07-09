package org.query.calc;

import gnu.trove.map.hash.TDoubleObjectHashMap;

import java.io.IOException;
import java.util.Arrays;

public final class ABCDataset {
    private final ABCRecord[] records;

    public ABCDataset(TuplesFileReader aReader) throws IOException {
        TDoubleObjectHashMap<ABCRecord> abc = new TDoubleObjectHashMap<>(aReader.getCount() / 2);
        int row = 0;
        while (aReader.hasNext()) {
            DoubleTuple aRecord = aReader.next();
            double a = aRecord.getV1();
            ABCRecord record = abc.get(aRecord.getV1());
            if (record == null) {
                record = new ABCRecord(row, a, 0);
                abc.put(a, record);
                row += 1;
            }
            double x = aRecord.getV2();
            record.increaseTotalX(x);
        }
        records = abc.values(new ABCRecord[0]);
        Arrays.sort(records, (a, b) -> Double.compare(a.getA(), b.getA()));
    }

    public ABCRecord[] getRecordsSortedByA() {
        return records;
    }
}
