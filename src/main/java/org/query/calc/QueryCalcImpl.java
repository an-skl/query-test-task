package org.query.calc;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;

public class QueryCalcImpl implements QueryCalc {
    private static final NumberFormat OUTPUT_FORMAT;
    public static final int MAX_RECORDS = 10;

    static {
        OUTPUT_FORMAT = NumberFormat.getInstance(Locale.US);
        OUTPUT_FORMAT.setMaximumFractionDigits(6);
        OUTPUT_FORMAT.setMinimumFractionDigits(6);
        OUTPUT_FORMAT.setGroupingUsed(false);
    }

    /**
     * Notes on the optimizations used:
     *
     * 1. BCDataset is a join of t2 and t3, having function calcSumYzProduct
     *    that returns the sum of the y*z products for every b+c that is
     *    greater than its argument. It does so via binary search of the first
     *    record with a b + c above the argument and then caching
     *    the products for the requested b + c and all the records having
     *    a greater b + c.
     * 2. The t1 dataset is not stored in memory and is processed in a
     *    single pass.
     *
     * Thus, the task's performance requirements are satisfied:
     * 1. The optimizations are tailored to computing time;
     * 2. Only two tables' join and the single result table is
     *    continuously kept in memory.
     *
     * An additional test case `case-4-ties-and-negatives` has been added
     * to verify the performance and test edge cases of the
     * implementation.
     *
     * Single additional dependency 'implementation 'net.sf.trove4j:trove4j:3.0.3'
     * has been added for the fast collections for primitive data types.
     */
    @Override
    public void select(Path t1, Path t2, Path t3, Path output) throws IOException {
        // - t1 is a file contains table "t1" with two columns "a" and "x". First line is a number of rows, then each
        //  line contains exactly one row, that contains two numbers parsable by Double.parse(): value for column a and
        //  x respectively.See test resources for examples.
        // - t2 is a file contains table "t2" with columns "b" and "y". Same format.
        // - t3 is a file contains table "t3" with columns "c" and "z". Same format.
        // - output is table stored in the same format: first line is a number of rows, then each line is one row that
        //  contains two numbers: value for column a and s.
        //
        // Number of rows of all three tables lays in range [0, 1_000_000].
        // It's guaranteed that full content of all three tables fits into RAM.
        // It's guaranteed that full outer join of at least one pair (t1xt2 or t2xt3 or t1xt3) of tables can fit into RAM.
        //
        // TODO: Implement following query, put a reasonable effort into making it efficient from perspective of
        //  computation time, memory usage and resource utilization (in that exact order). You are free to use any lib
        //  from a maven central.
        //
        // SELECT a, SUM(x * y * z) AS s FROM 
        // t1 LEFT JOIN (SELECT * FROM t2 JOIN t3) AS t
        // ON a < b + c
        // GROUP BY a
        // STABLE ORDER BY s DESC
        // LIMIT 10;
        // 
        // Note: STABLE is not a standard SQL command. It means that you should preserve the original order. 
        // In this context it means, that in case of tie on s-value you should prefer value of a, with a lower row number.
        // In case multiple occurrences, you may assume that group has a row number of the first occurrence.

        BCDataset bcDataset;
        try (TuplesFileReader bReader = TuplesFileReader.open(t2);
             TuplesFileReader cReader = TuplesFileReader.open(t3)) {
            bcDataset = new BCDataset(bReader, cReader);
        }

        ABCDataset abcDataset;
        try(TuplesFileReader aReader = TuplesFileReader.open(t1)) {
            abcDataset = new ABCDataset(aReader);
        }

        BCRecord[] bc = bcDataset.getRecordsSortedByBPlusC();
        ABCRecord[] abc = abcDataset.getRecordsSortedByA();

        Double2ObjectRBTreeMap<List<ABCRecord>> resultMap = new Double2ObjectRBTreeMap<>(DoubleComparators.OPPOSITE_COMPARATOR);
        double lastItemXyzProduct = Double.NEGATIVE_INFINITY;

        int bcIndex = 0;
        for (int aIndex = 0; aIndex < abc.length && (bcIndex < bc.length || resultMap.size() < MAX_RECORDS); aIndex++) {
            ABCRecord abcRecord = abc[aIndex];
            double a = abcRecord.getA();
            bcIndex = bcDataset.findRecordIndexWithBPlusCGreaterThan(a, bcIndex, bc.length);
            BCRecord bcRecord = null;
            double sumXyzProduct = 0;
            if (bcIndex < bc.length) {
                bcRecord = bc[bcIndex];
                sumXyzProduct = abcRecord.getTotalX() * bcRecord.getSumYzProduct();
            }
            abcRecord.setSumXyzProduct(sumXyzProduct);

            if (resultMap.size() == MAX_RECORDS) {
                if (lastItemXyzProduct < sumXyzProduct) {
                    resultMap.remove(lastItemXyzProduct);
                } else {
                    double remainingMaxSum = bcRecord == null ? Double.NEGATIVE_INFINITY :
                        Math.max(
                            abcRecord.getMaxTotalX() * bcRecord.getMaxSumYzProduct(),
                            abcRecord.getMinTotalX() * bcRecord.getMinSumYzProduct()
                        );
                    if (remainingMaxSum < lastItemXyzProduct) {
                        // If there's definitely no higher SUM(X*Y*Z) then stop iterating.
                        break;
                    } else {
                        continue;
                    }
                }
            }

            List<ABCRecord> records = resultMap.get(sumXyzProduct);
            if (records == null) {
                records = new ArrayList<>(3);
                resultMap.put(sumXyzProduct, records);
            }
            records.add(abcRecord);
            if (resultMap.size() == MAX_RECORDS) {
                lastItemXyzProduct = resultMap.lastDoubleKey();
            }
        }

        List<ABCRecord> result = new ArrayList<>(MAX_RECORDS);
        for (List<ABCRecord> ties : resultMap.values()) {
            if (result.size() < MAX_RECORDS) {
                ties.sort((a, b) -> Integer.compare(a.getRowNumber(), b.getRowNumber()));
                result.addAll(ties);
            } else {
                break;
            }
        }
        result = result.subList(0, Math.min(result.size(), MAX_RECORDS));

        writeOutput(output, result);
    }

    private static void writeOutput(Path output, List<ABCRecord> result) throws IOException {
        Files.write(output, () -> new Iterator<CharSequence>() {
            private int linePosition = 0;
            private final StringBuilder text = new StringBuilder(50);

            @Override
            public boolean hasNext() {
                return linePosition <= result.size()
                    && linePosition <= MAX_RECORDS; // <= because the first row is the count
            }

            @Override
            public CharSequence next() {
                if (linePosition == 0) {
                    text.append(Math.min(result.size(), MAX_RECORDS));
                } else {
                    text.delete(0, text.length());
                    ABCRecord record = result.get(linePosition - 1); // - 1 because the first row is the count
                    text.append(OUTPUT_FORMAT.format(record.getA()))
                        .append(' ')
                        .append(OUTPUT_FORMAT.format(record.getSumXyzProduct()));
                }
                //System.out.println(text);
                linePosition += 1;
                return text;
            }
        });
    }
}
