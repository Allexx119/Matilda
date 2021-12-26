package ru.asb.program.bridge.util;

import java.io.*;
import java.util.ArrayList;

import java.nio.charset.StandardCharsets;

/**
 * Класс для работы с файлами
 * */
public class FileUtil {
	/**
	 * Метод построчного чтения файлов
	 * Возвращает список строк в файле
	 * */
	public static ArrayList<String> readLineFile(String filePath) throws IOException {
		String line;
		ArrayList<String> list = new ArrayList<>();
		Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(reader);
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		return list;
	}

	/**
	 * Метод посимвольного чтения файла
	 * Возвращает строку
	 * */
	public static String readFile(String filePath) throws IOException {
		StringBuilder builder = new StringBuilder();
		Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(reader);
		int symbol;
		while ((symbol = br.read()) != -1) {
			builder.append((char)symbol);
		}
		return builder.toString();
	}

	/**
	 * Запись строки в файл
	 * */
	public static void writeFile(String toOutput, String filePath, boolean append) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(filePath, append);
		fileOutputStream.write(toOutput.getBytes());
		fileOutputStream.close();
	}

	/**
	 * Метод очистки файла
	 * */
	public static void clearFile(String filePath) throws FileNotFoundException, IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(filePath, false);
		fileOutputStream.write("".getBytes());
		fileOutputStream.close();
	}
}
