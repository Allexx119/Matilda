package ru.asb.program.bridge.util;

import com.opencsv.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс работы с .CSV файлами
 * */
public class CSV {
    private char separator = ';';
    private char quoteChar = '"';
    private boolean withHeader = true;
    private String filePath;
    private String[] header;
    private List<String[]> csv;

    public CSV() {
        this.csv = new ArrayList<>();
    }

    public CSV(List<String> list, boolean withHeader) {
        this.csv = new ArrayList<>();
        this.withHeader = withHeader;
        for (String val : list) {
            String[] valArr = {val};
            this.csv.add(valArr);
        }
        if (withHeader) {
            this.header = this.csv.get(0);
            this.csv.remove(0);
        }
    }

    public CSV(String[] arr, boolean withHeader) {
        this.csv = new ArrayList<>();
        this.withHeader = withHeader;
        for (String val : arr) {
            String[] valArr = {val};
            this.csv.add(valArr);
        }
        if (withHeader) {
            this.header = this.csv.get(0);
            this.csv.remove(0);
        }
    }

    public CSV(String[][] arr, boolean withHeader) {
        this.csv = new ArrayList<String[]>();
        this.withHeader = withHeader;
        for (String[] row : arr) {
            this.csv.add(row);
        }
        if (withHeader) {
            this.header = this.csv.get(0);
            this.csv.remove(0);
        }
    }

    public CSV(CSV csv, int... columnNumbers) {
        this.csv = new ArrayList<String[]>();
        String[] line = new String[columnNumbers.length];
        for (int i = 0; i < columnNumbers.length; i++) {
            line[i] = csv.getHeader(columnNumbers[i]);
        }
        this.header = line;

        for (int i = 0; i < csv.rowCount(); i++) {
            this.csv.add(csv.getLine(i, columnNumbers));
        }

    }
    /**
     * Возвращает массив строк (по указанным колонкам columnNumbers) из csv файла по номеру строки rowNumber
     * */
    private String[] getLine(int rowNumber, int... columnNumbers) {
        String [] line = new String [columnNumbers.length];
        int i = 0;
        for (int colIndex : columnNumbers) {
            line[i] = this.getColumn(colIndex).get(rowNumber);
            i++;
        }
        return line;
    }
    /**
     * Возвращает число строк в csv файле
     * */
    public int rowCount() {
        if (withHeader) {
            return this.csv.size()+1;
        }
        return this.csv.size();
    }

    /**
     * Возвращает число колонок в csv файле
     * */
    public int columnCount() {
        return this.csv.get(0).length;
    }

    /**
     * Устанавливает разделить при чтении .csv файла
     * separator - символ разделителя
     * */
    public CSV withSeparator(char separator) {
        this.separator = separator;
        return this;
    }
    /**
     * Устанавливает ограничитель одной записи в .csv файле
     * quoteChar - символ ограничителя (чаще всего ковычки)
     * */
    public CSV withQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    /**
     * Определяет обработку headerа .csv файла
     * */
    public CSV withHeader(boolean include) {
        this.withHeader = include;
        return this;
    }
    /**
     * Устанавливает откуда читать .csv файл
     * */
    public CSV fromFile(String filePath) {
        this.filePath = filePath;
        return this;
    }
    /**
     * Устанавливает куда писать .csv файл
     * */
    public CSV toFile(String filePath) {
        this.filePath = filePath;
        return this;
    }
    /**
     * Читает .сsv файл по установленным параметрам
     * */
    public CSV read() throws IOException {
        FileReader csvFileReader = new FileReader(filePath);
        CSVParser parser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quoteChar).build();
        CSVReader csvReader = new CSVReaderBuilder(csvFileReader).withCSVParser(parser).build();
        this.csv = csvReader.readAll();
        if (withHeader) {
            header = this.csv.remove(0);
        }
        csvFileReader.close();
        return this;
    }
    /**
     * Пишет .сsv файл по установленным параметрам
     * */
    public CSV write() throws IOException {
        FileWriter csvFileWriter = new FileWriter(filePath);
        CSVParser parser = new CSVParserBuilder().withSeparator(separator).withQuoteChar(quoteChar).build();
        ICSVWriter csvWriter = new CSVWriterBuilder(csvFileWriter).withParser(parser).build();
        List<String[]> list = new LinkedList<String[]>(this.csv);
        if (withHeader) {
            list.add(0, this.header);
        }
        csvWriter.writeAll(list);
        csvFileWriter.close();
        return this;
    }
    /**
     * Возвращает массив строк с хедерами
     * */
    public String[] getHeader() {
        return this.header;
    }
    /**
     * Возвращает true при налаичия хедера, указанного в name
     * */
    public boolean haveHeader(String name) {
        boolean have = false;
        for (String hdr : this.header){
            if (hdr.equals(name)) {
                have = true;
                break;
            }
        }
        return have;
    }
    /**
     * Возвращает название колонки по номеру колонки (начиная с нуля)
     * */
    public String getHeader(int index) {
        return this.header[index];
    }
    /**
     * Возвращает номер колонки (начиная с нуля) по имени колонки
     * */
    public int getIndex(String headerName) {
        int i = 0;
        for (; i < this.header.length; i++) {
            if (headerName.equals(this.header[i])) break;
        }
        return i;
    }
    /**
     * Возвращает колонку по номеру колонки (начиная с нуля)
     * */
    public List<String> getColumn(int index) {
        List<String> column = new ArrayList<String>();
        if (withHeader) {
            column.add(header[index]);
        }
        for (String [] line : csv) {
            column.add(line[index]);
        }
        return column;
    }
    /**
     * Возвращает колонку по имени колонки
     * */
    public List<String> getColumn(String header) {
        return getColumn(getIndex(header));
    }
    /**
     * Возвращает список массивов значений .csv файла с хедером или без в зависимости от установленных параметров
     * */
    public List<String[]> getValues() {
        List<String[]> list = new ArrayList<String[]>(this.csv);
        if (this.withHeader) {
            list.add(0, this.header);
        }
        return list;
    }
    /**
     *
     * */
    public void setHeader(String[] header) {
        this.header = header;
    }

    /**
     * Добавить строку в CSV
     * */
    public void addRow(String[] row) {
        csv.add(row);
    }
}

