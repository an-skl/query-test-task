package org.query.calc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

final class TuplesFileReader implements Closeable {
    private final int count;
    private final InputStream inputStream;
    private final StringBuilder text = new StringBuilder(30);
    private DoubleTuple nextValue;

    public TuplesFileReader(int count, InputStream inputStream) {
        this.count = count;
        this.inputStream = inputStream;
    }

    public int getCount() {
        return count;
    }

    public boolean hasNext() throws IOException {
        Double v1 = readDouble();
        if (v1 == null) {
            nextValue = null;
            return false;
        }
        Double v2 = readDouble();
        if (v2 == null) {
            nextValue = null;
            throw new IOException("Invalid file format: only one value found whereas expected two.");
        }
        nextValue = new DoubleTuple(v1, v2);
        return true;
    }

    public DoubleTuple next() throws IOException {
        if (nextValue == null) {
            throw new EOFException("No more records in the processed file.");
        }
        return nextValue;
    }

    private Double readDouble() throws IOException {
        text.delete(0, text.length());
        readNonWhitespaceValue(text, inputStream);
        if (text.length() > 0) {
            return Double.parseDouble(text.toString());
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public static TuplesFileReader open(Path path) throws IOException {
        InputStream inputStream = Files.newInputStream(path);
        BufferedInputStream in = new BufferedInputStream(inputStream, 4 * 1024 * 1024);
        StringBuilder countReader = new StringBuilder(7);
        readNonWhitespaceValue(countReader, in);
        int count = Integer.parseInt(countReader, 0, countReader.length(), 10);
        return new TuplesFileReader(count, in);
    }

    private static void readNonWhitespaceValue(StringBuilder text, InputStream in) throws IOException {
        while (text.length() < 128) {
            int value = in.read();
            if (value == -1) {
                return;
            }
            boolean isWhitespace = Character.isWhitespace(value);
            if (!isWhitespace) {
                text.append((char) value);
            } else if (text.length() != 0) {
                return;
            }
        }
    }
}
