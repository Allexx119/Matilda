package ru.asb.program;

import ru.asb.program.bridge.database.MySQLJDBC;
import ru.asb.program.bridge.gui.GUI;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.settings.Settings;
import ru.asb.program.bridge.util.Log;
import ru.asb.program.operation.report.ReportsTables;
import ru.asb.program.operation.report.DataForDB;
import ru.asb.program.operation.user.UserGetAll;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Program {
	public Program( String[] args) {
		try {
			Settings.get().load();
			switch(args[0]) {
				case("-gui") : GUI.runGui(); break;
				case("-console") : startConsole(args); break;
				case("-help") : showHelp(); break;
			}
		} catch (UnknownHostException ue) {
			showHelp();
			System.out.println("Matilda stops working");
		}
	}

	private void startConsole(String[] args) throws UnknownHostException {
		Log.info("Matilda start working in console");
		String[] sapboConnection = Settings.get().getSapboConnections().get(Integer.parseInt(args[1]));
		String ip = sapboConnection[1];
		String username = sapboConnection[2];
		String password = sapboConnection[3];
		String authType = sapboConnection[4];
		BOConnection.setLogonInfo(ip, username, password, authType);

		String[] mysqlConnection = Settings.get().getDbConnectionsMap().get("mysql");
		ip = mysqlConnection[1];
		String port = mysqlConnection[2];
		String dbName = mysqlConnection[3];
		username = mysqlConnection[4];
		password = mysqlConnection[5];
		MySQLJDBC.setLogonInfo(ip, port, dbName, username, password);

//		if(args[2].equals("-u") && args[3].equals("user")&& args[4].equals("dsc")){
//			Thread executeUserSetDsc = new Thread(new UserSetDsc());
//			executeUserSetDsc.start();
//		}
//		else
		if(args[2].equals("-r") && args[3].equals("report")&& args[4].equals("db") && args[5].equals("info")){	//Выгрузка всей информации по отчетам, связанным юневерсам в базу КАРЕН (необходимо доработать для UNX)
			String tableName = Settings.get().getDbTablesList("ReportGetDataForDB").get(Integer.parseInt(args[6]));
			Thread executeReportGetDataForDB = new Thread(new DataForDB(tableName));
			executeReportGetDataForDB.start();
		}
		else
		if(args[2].equals("-r") && args[3].equals("user")&& args[4].equals("get") && args[5].equals("all")){	//Выгрузка всей информации по пользователям в БД карен
			String tableName = Settings.get().getDbTablesList("UserGetAll").get(Integer.parseInt(args[6]));
			Thread executeUserGetAll = new Thread(new UserGetAll(tableName));
			executeUserGetAll.start();
		}
//		else if(args[2].equals("-r") && args[3].equals("report")&& args[4].equals("sql") && args[5].equals("find")){	//
//			Thread executeReportGetSqlTable = new Thread(new ReportGetSqlTable());
//			executeReportGetSqlTable.start();
//		}
		else
		if(args[2].equals("-r") && args[3].equals("report") && args[4].equals("sql") && args[5].equals("tables")) {	//Поиск таблиц в отчетах SAP BO (в каких отчетах используется таблица)
			boolean withSQL = args[6].equals("1");
			List<String> tablesFilter = Settings.get().getTablesFilter();
			Thread executeReportFindSqlTable = new Thread(new ReportsTables(tablesFilter, withSQL));
			executeReportFindSqlTable.start();
		}
		else{
			Log.info("Matilda stops working. Incorrect arguments income.");
			showHelp();
		}
	}

	private void showHelp() {
		try {
			for (String line : Files.readAllLines(Paths.get("readme.txt"))) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exitProgram() {
		System.out.println("Program finished correctly.");
		System.exit(0);
	}
}
