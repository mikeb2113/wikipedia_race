import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ParquetToText {

    /**
     * Usage:
     *   java ParquetToText input.parquet output.txt
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java ParquetToText <input.parquet> <output.txt>");
            System.exit(1);
        }

        String parquetPath = args[0];
        String outputPath = args[1];

        try {
            dumpParquet(parquetPath, outputPath);
            System.out.println("Wrote human-readable text to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error reading parquet file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void dumpParquet(String parquetFile, String outputFile) throws IOException {
        Path path = new Path(parquetFile);
        Configuration conf = new Configuration();

        try (ParquetFileReader reader = ParquetFileReader.open(
                     HadoopInputFile.fromPath(path, conf));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            ParquetMetadata meta = reader.getFooter();
            MessageType schema = meta.getFileMetaData().getSchema();

            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);

            // Loop through row groups
            for (org.apache.parquet.hadoop.metadata.BlockMetaData block : meta.getBlocks()) {
                // Create a RecordReader for this row group
                RecordReader<Group> recordReader =
                        columnIO.getRecordReader(reader.readNextRowGroup(), new GroupRecordConverter(schema));

                Group group;
                long rowCount = block.getRowCount();

                for (long i = 0; i < rowCount; i++) {
                    group = recordReader.read();
                    String line = groupToString(group);
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Simple human-readable formatting.
     * You can customize this to CSV, JSON, etc.
     */
    private static String groupToString(Group group) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");

        int fieldCount = group.getType().getFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            String fieldName = group.getType().getFieldName(i);
            int repeatCount = group.getFieldRepetitionCount(i);

            sb.append("\"").append(fieldName).append("\": ");

            if (repeatCount == 0) {
                sb.append("null");
            } else if (repeatCount == 1) {
                sb.append(valueToString(group, i, 0));
            } else {
                // repeated field – print as array
                sb.append("[");
                for (int j = 0; j < repeatCount; j++) {
                    if (j > 0) sb.append(", ");
                    sb.append(valueToString(group, i, j));
                }
                sb.append("]");
            }

            if (i < fieldCount - 1) {
                sb.append(", ");
            }
        }

        sb.append(" }");
        return sb.toString();
    }

    private static String valueToString(Group group, int fieldIndex, int repetitionIndex) {
        switch (group.getType().getType(fieldIndex).asPrimitiveType().getPrimitiveTypeName()) {
            case BINARY:
                // interpret as UTF-8 string by default
                return "\"" + group.getBinary(fieldIndex, repetitionIndex).toStringUsingUTF8() + "\"";
            case INT32:
                return String.valueOf(group.getInteger(fieldIndex, repetitionIndex));
            case INT64:
                return String.valueOf(group.getLong(fieldIndex, repetitionIndex));
            case DOUBLE:
                return String.valueOf(group.getDouble(fieldIndex, repetitionIndex));
            case FLOAT:
                return String.valueOf(group.getFloat(fieldIndex, repetitionIndex));
            case BOOLEAN:
                return String.valueOf(group.getBoolean(fieldIndex, repetitionIndex));
            case INT96:
                // often timestamps; just print raw for simplicity
                return "\"" + group.getInt96(fieldIndex, repetitionIndex).toString() + "\"";
            default:
                // fallback
                return "\"" + group.getValueToString(fieldIndex, repetitionIndex) + "\"";
        }
    }
}