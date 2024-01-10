package com.api.calculator.stockprice;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PDFCheckerTestUtil {

    public List<String[]> readCheckCsv(String fileUri, String split){
        List<String[]> records = new ArrayList<>();
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(fileUri));
            while ((line = br.readLine()) != null) {
                records.add(line.split(split));
            }
        } catch (IOException e) {
            System.out.println("Error when open "+fileUri+".: "+e.getMessage());
            return new ArrayList<>();
        }
        return records;
    }

    public String readPdfContentText(String fileUri) {

        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileUri));
            String line;

            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null){
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public void createCheckerFile(String fileUri, List<String[]> dataLines) throws IOException {

        File csvOutputFile = new File(fileUri);
        if(csvOutputFile.exists()){
            csvOutputFile.delete();
        }
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

    public void saveTextFile(String uri, String content) {

        File textOutputFile = new File(uri);
        if (textOutputFile.exists()) {
            textOutputFile.delete();
        }

        try {
            PrintWriter printWriter = new PrintWriter(textOutputFile);
            printWriter.println(content);
            printWriter.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
