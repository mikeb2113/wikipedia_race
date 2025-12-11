package com.example.persistence;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParquetToTextTest {

    @Test
    void dumpParquet_writesHumanReadableTextFile() throws Exception {
        // Create temp directory
        java.nio.file.Path parquetFile = Paths.get("target/sample.parquet");
        java.nio.file.Path outputFile  = Paths.get("target/sample_output.txt");
        java.nio.file.Path tempDir = Files.createTempDirectory("parquet-test");
        //java.nio.file.Path parquetFile = tempDir.resolve("test.parquet");
        //java.nio.file.Path outputFile = tempDir.resolve("test.txt");

        // 1. Create small parquet file
        writeSampleParquet(parquetFile.toString());

        // 2. Convert parquet → text
        ParquetToText.dumpParquet(parquetFile.toString(), outputFile.toString());

        // 3. Validate output
        assertTrue(Files.exists(outputFile), "Output file should exist");

        List<String> lines = Files.readAllLines(outputFile, StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty(), "Output text should not be empty");

        String all = String.join("\n", lines);

        // Check content
        assertTrue(all.contains("Alice"), "Should contain row with Alice");
        assertTrue(all.contains("Bob"), "Should contain row with Bob");
    }

    private void writeSampleParquet(String filePath) throws Exception {
        // Schema: id:int32, name:utf8
        String schemaString = "message test_schema {\n" +
                "  required int32 id;\n" +
                "  required binary name (UTF8);\n" +
                "}";

        MessageType schema = MessageTypeParser.parseMessageType(schemaString);

        Configuration conf = new Configuration();
        GroupWriteSupport.setSchema(schema, conf);

        Path parquetPath = new Path(filePath);

        try (ParquetWriter<Group> writer =
                     ExampleParquetWriter.builder(parquetPath)
                             .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                             .withConf(conf)
                             .build()) {

            SimpleGroupFactory factory = new SimpleGroupFactory(schema);

            Group g1 = factory.newGroup()
                    .append("id", 1)
                    .append("name", "Alice");

            Group g2 = factory.newGroup()
                    .append("id", 2)
                    .append("name", "Bob");

            writer.write(g1);
            writer.write(g2);
        }
    }
}