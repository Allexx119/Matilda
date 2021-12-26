package ru.asb.program.bridge.settings;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.asb.program.bridge.database.MySQLJDBC;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.util.*;

public class Settings {
	private static Settings settings = new Settings();
	private Map<String, String> tasks;
	private Map<String, String> savings = new HashMap<>();
	private Map<String, List<String>> dbTables = new HashMap<>();
	private List<String> tablesFilter;
	private List<String[]> sapboConnections;
	private List<String[]> dbConnections;
	private String sapboConnectionName;
	private String dbConnectionName;

	private Settings() {}

	public void save(String filepath, List<String[]> connectionList) {
		try {
			String header = Files.readAllLines(Paths.get(filepath)).get(0);
			StringBuilder file = new StringBuilder(header).append("\n");
			for (String[] arr : connectionList) {
				StringBuilder line = new StringBuilder();
				for (String elem : arr) {
					line.append(elem).append(";");
				}
				file.append(line.substring(0, line.length()-1)).append("\n");
			}
			Files.newBufferedWriter(Paths.get(filepath), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			Files.write(Paths.get(filepath), file.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		try {
			tasks = defineTasks();
			sapboConnections = loadConnections("settings\\SAPBO_connections.param");
			dbConnections = loadConnections("settings\\DB_connections.param");
			loadSavings("settings\\settings.param");
			tablesFilter = loadTablesFilter();
		} catch(IOException ioe) {
			System.out.println("Unable to load settings!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDefaultConnectionsParameters() {
		try {
			sapboConnectionName = savings.get("sapbo");
			dbConnectionName = savings.get("db");
			String[] sapboConnection = getMap(sapboConnections).get(sapboConnectionName);
			String[] dbConnection = getMap(dbConnections).get(dbConnectionName);

			//Установка текущих подключений к базам
			BOConnection.setLogonInfo(
					sapboConnection[1],
					sapboConnection[2],
					sapboConnection[3],
					sapboConnection[4]
			);
			MySQLJDBC.setLogonInfo(
					dbConnection[1],
					dbConnection[2],
					dbConnection[3],
					dbConnection[4],
					dbConnection[5]
			);
		} catch(UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static Settings get() {
		return settings;
	}

	private Map<String, String> defineTasks() {
		Map<String, String> map = new HashMap<>();
		map.put("UserGetAll", "Выгрузка всей информации по пользователям");
		map.put("ReportGetDataForDB", "Выгрузка информации по отчетам");
		map.put("ReportFindSqlTable", "Поиск таблиц в отчетах SAP BO");
		map.put("UniverseTables", "Поиск таблиц в юниверсах SAP BO");
		return map;
	}
		
	private List<String> loadTablesFilter() throws IOException {
		CSV tablesFilter = new CSV().fromFile("settings\\table_filter.csv").withHeader(false).withSeparator(';').withQuoteChar('"').read();
		List<String> tablesList = new ArrayList<>();
		for (String[] row : tablesFilter.getValues()) {
			tablesList.add(row[0]);
		}
		return tablesList;
	}

	/**
	 * Загружает данные из файлов с данными для соединения с базами данных
	 * */
	private List<String[]> loadConnections(String filePath) throws IOException {
		List<String[]> sapboConnections = new ArrayList<>();
		List<String> list = FileUtil.readLineFile(filePath/*"preferences\\SAPBO_connections.param"*/);
		for (String line : list) {
			if (!line.contains("[")) {
				String[] connection = line.split(";");
				sapboConnections.add(connection);
			}
		}
		return sapboConnections;
	}

	/**
	 *
	 * */
	private void loadSavings(String filePath) throws IOException {
		List<String> list = FileUtil.readLineFile(filePath);
		for (String line : list) {
			if (!line.contains("[") && !line.isEmpty()) {
				String[] set = line.split(":");
				switch(set[0]) {
					case ("CONNECTION") : savings.put(set[1], set[2]); break;
					case ("DB_TABLE") : loadDbTables(set); break;
				}
			}
		}
	}
	/**
	 *
	 * */
	private void loadDbTables(String[] set) {
		if (dbTables.containsKey(set[1].toUpperCase())) {
			dbTables.get(set[1].toUpperCase()).add(set[2]);
		} else {
			List<String> values = new ArrayList<>();
			values.add(set[2]);
			dbTables.put(set[1].toUpperCase(), values);
		}
	}

	/**
	 * Возвращает MAP<String, String[]> на основе списка List<String[]> list
	 * */
	private Map<String, String[]> getMap(List<String[]> list) {
		Map<String, String[]> map = new HashMap<>();
		for (String[] line : list) {
			map.put(line[0], line);
		}
		return map;
	}
	
	public Map<String, String> getTasks() {
		return this.tasks;
	}
	
	public List<String> getTablesFilter() {
		return this.tablesFilter;
	}

	public List<String[]> getSapboConnections() {
		return sapboConnections;
	}

	public List<String[]> getDbConnections() {
		return dbConnections;
	}

	public Map<String, String[]> getSapboConnectionsMap() {
		return getMap(sapboConnections);
	}

	public Map<String, String[]> getDbConnectionsMap() {
		return getMap(dbConnections);
	}

	public List<String> getDbTablesList(String taskName) {
		return dbTables.get(taskName.toUpperCase());
	}

	public ArrayList<String> getConnectionsNames(List<String[]> list) {
		ArrayList<String> names = new ArrayList<>();
		for (String[] line : list) {
			names.add(line[0]);
		}
		return names;
	}

	public String getSapboConnectionName() {
		return sapboConnectionName;
	}

	public String getDbConnectionName() {
		return dbConnectionName;
	}
}
