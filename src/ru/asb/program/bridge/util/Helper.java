package ru.asb.program.bridge.util;

//import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	public static String prepareSQL(String sqlString) {
		String sql = sqlString;
		sql = Helper.removeMultiLineComments(sql);
		sql = Helper.removeSingleLineComments(sql);
		sql = Helper.clearSpaces(sql);
		sql = Helper.clearWithUr(sql);
		sql = Helper.daysToDay(sql);
		sql = Helper.normalizeBrackets(sql);
		sql = Helper.clearEnters(sql);
		sql = sql.trim();
		return sql;
	}

	public static String prepareReportSQL(String sqlString) {
		String sql = sqlString;
		sql = Helper.removeMultiLineComments(sql);
		sql = Helper.removeSingleLineComments(sql);
		sql = Helper.clearSpaces(sql);
//		sql = Helper.clearWithUr(sql);
//		sql = Helper.daysToDay(sql);
		sql = Helper.normalizeBrackets(sql);
		sql = Helper.clearEnters(sql);
		sql = sql.trim();
		return sql;
	}

	private static String clearSpaces(String sql) {
		Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql = matcher.replaceAll(" ");
		return sql;
	}

	private static String clearEnters(String sql) {
		Pattern pattern = Pattern.compile("[\\n\\r]+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql = matcher.replaceAll(" ");
		return sql;
	}
	
	public static String clearRussianChars(String sql) {
		Pattern pattern = Pattern.compile("[а-яА-Я]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		return matcher.replaceAll("");
	}

	private static String clearWithUr(String sql) {
		Pattern pattern = Pattern.compile("\\s+\\bwith\\b\\s+\\bur\\b", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql =  matcher.replaceAll("");

		pattern = Pattern.compile("\\bFOR READ ONLY\\b", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sql);
		sql =  matcher.replaceAll("");

		pattern = Pattern.compile("\\bAS JOIN\\b", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sql);
		sql =  matcher.replaceAll("");

		return sql;
	}

	private static String daysToDay(String sql) {
		Pattern pattern = Pattern.compile("\\bdays\\b", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql = matcher.replaceAll("DAY");

		pattern = Pattern.compile("\\bmonths\\b", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sql);
		sql = matcher.replaceAll("MONTH");

		pattern = Pattern.compile("\\byears\\b", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sql);
		sql = matcher.replaceAll("YEAR");

		return sql;
	}

	private static String normalizeBrackets(String sql) {
		Pattern pattern = Pattern.compile("\\(\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql = matcher.replaceAll("\\(");
		
		pattern = Pattern.compile("\\s+\\)", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(sql);
		return matcher.replaceAll("\\)");	
	}

	private static String removeMultiLineComments(String sql) {
		Pattern pattern = Pattern.compile("\\/\\*.*?\\*\\/", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(sql);
		return matcher.replaceAll("");
	}

	private static String removeSingleLineComments(String sql) {
		//С начала строки
		Pattern pattern = Pattern.compile("^\\s*--.*?\\n", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		sql = matcher.replaceAll("");

		//Не с начала строки
		pattern = Pattern.compile("--.*?(?=\\n)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		matcher = pattern.matcher(sql);
		return matcher.replaceAll("");
	}

	public static List<String[]> findParams(List<String> sqls) {
		List<String[]> list = new ArrayList<String[]>();
		Pattern pattern = Pattern.compile("(#.*?#)", Pattern.CASE_INSENSITIVE);
		for (String sql : sqls) {
			Matcher matcher = pattern.matcher(sql);
			while (matcher.find()) {
				if (!contains(matcher.group(1), list)) {
					String[] line = {matcher.group(1), ""};
					list.add(line);
				}
			}
		}
//		for (String []line : list) {
//			System.out.println(line[0]);
//		}
		return list;
	}

	public static List<String[]> findAndReplaceParams(List<String[]> reports, List<String[]> replaceMap) {
		List<String[]> changedReports = new ArrayList<>();
		List<String> unknownParameters = new ArrayList<>();
		for (String[] pair : reports) {
			String sql = pair[1];
			Matcher matcher = Pattern.compile("(\\p{Sm}{0,1})\\s*(.{0,1})(#[\\w\\.]*?#)(.{0,1})", Pattern.CASE_INSENSITIVE).matcher(sql);
			while (matcher.find()) {
				String math = matcher.group(1);
				String parameter = matcher.group(3);
				String beforeParameterSymbol = matcher.group(2);
				String afterParameterSymbol = matcher.group(4);


				String value = getValue(parameter, replaceMap);
				if (value != null) {
					String beforeValueSymbol = Character.toString(value.charAt(0));
					String afterValueSymbol = Character.toString(value.charAt(value.length()-1));
					if (!math.equals("")) {
						value = value.replaceFirst(math, "");
					}
					if (beforeParameterSymbol.equals(beforeValueSymbol) && afterParameterSymbol.equals(afterValueSymbol)) {
						sql = sql.replaceAll(parameter, value.substring(1, value.length()-1));
					} else {
						sql = sql.replaceAll(parameter, value);
					}
				} else {
					unknownParameters.add("\"" + parameter + "\",\"\"");
				}
			}
			String[] changedPair = {pair[0], sql};
			changedReports.add(changedPair);

//			if (unknownParameters.size() > 0) {
//				System.out.println(sql);
//				for (String param : getUniqueList(unknownParameters)) {
//					System.out.println(param);
//				}
//				System.out.println("^---------------------------------------------------------^");
//				unknownParameters.clear();
//			}
		}
		if (unknownParameters.size() > 0) {
			System.out.println("Список неизвестных параметров:");
			for (String param : getUniqueList(unknownParameters)) {
				System.out.println(param);
			}
		}
		return changedReports;
	}

	private static String getValue(String parameter, List<String[]> map) {
		for (String[] pair : map) {
			Matcher matcher = Pattern.compile(pair[0], Pattern.CASE_INSENSITIVE).matcher(parameter);
			if (matcher.find()) {
				return pair[1];
			}
		}
		return null;
	}

	private static List<String> getUniqueList(List<String> rawList) {
		ArrayList<String> uniqueList = new ArrayList<>();
		for (String string : rawList) {
			if (!contains(string, uniqueList)) {
				uniqueList.add(string);
			}
		}
		return uniqueList;
	}

//	private static void findAndReplaceParams(CSV csv, String colHeader, List<String[]> replaceMap) {
//		int colNum = csv.getIndex(colHeader);
//		for (int i = 0; i < replaceMap.size(); i++) {
//			if (!replaceMap.get(i)[1].isEmpty()) {
//				Pattern pattern = Pattern.compile(replaceMap.get(i)[0], Pattern.CASE_INSENSITIVE);
//				for (String[] line : csv.getValues()) {
//					String sql = line[colNum];
//					Matcher matcher = pattern.matcher(sql);
//					sql = matcher.replaceAll(replaceMap.get(i)[1]);
//					line[colNum] = sql;
//				}
//			}
//		}
//	}

//	public static void autoFindAndReplaceParams(CSV csv, String colHeader, List<String[]> replaceMap){
//		List<String[]> _replaceMap = new ArrayList<>();
//		for (String[] pair : replaceMap) {
//			String[] _pair = {pair[0], "\'PARAMETER\'"};
//			String[] regex = {
//					"#psConnectionsDWH.*?#",	//0
//					"#psConnectionsMART.*?#",	//1
//					"#.*?DATAHUB.*?#",	//2
//					"#.*?(?:(?:_|\\b)DATE(?:_|\\b)|(?:_|\\b)DT(?:_|\\b)).*?#",	//3
//					"#.*?(?:_|\\b)LIST(?:_|\\b).*?#",	//4
//					"#.*?(?:_|\\b)ID(?:_|\\b).*?#",			//5
//					"#.*?LOAD_TYPE.*?#"
//			};
//			for (int i = 0; i < regex.length; i++) {
//				Matcher matcher = Pattern.compile(regex[i], Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(_pair[0]);
//				if (matcher.find()) {
//					switch (i) {
//						case 0: _pair[1] = "DWH_SCHEMA"; break;
//						case 1: _pair[1] = "MART_SCHEMA";  break;
//						case 2: _pair[1] = "DATAHUB_SCHEMA";  break;
//						case 3: _pair[1] = "2020-01-01";  break;
//						case 4: _pair[1] = "(7,4,8,9,24,10,912)"; break;
//						case 5: _pair[1] = "12345";  break;
//						case 6: _pair[1] = "\'FULL\'";  break;
//						default: _pair[1] = "\'PARAMETER\'"; break;
//					}
//					break;
//				}
//			}
//			_replaceMap.add(_pair);
//		}
//		findAndReplaceParams(csv, colHeader, _replaceMap);
//	}



//	public static List<String> separateSQL(String sql) {
//		List<String> fullSql = new ArrayList<String>();
//		Pattern pattern = Pattern.compile("(?:\\p{P}*)[\\S]+?(?:\\p{P}{1,2}|\\s+|$)\\s*", Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(sql);
//		while (matcher.find()) {
//			fullSql.add(matcher.group(0));
//		}
//		return fullSql;
//	}

//	public static List<Text> separateSQL(String sql) {
//		List<Text> fullSql = new ArrayList<>();
//		Pattern pattern = Pattern.compile("([А-Я\\w\\:\\'\\-]+)?([^А-Я\\w\\:\\']+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
//		Matcher matcher = pattern.matcher(sql);
//		while (matcher.find()) {
//			fullSql.add(new Text(matcher.group(1)));
//			fullSql.add(new Text(matcher.group(2)));
//		}
//		return fullSql;
//	}


//	public static boolean detectSingleLineComments(String sql) {
//		Pattern pattern = Pattern.compile("\\s*--\\s*", Pattern.CASE_INSENSITIVE);
//		Matcher matcher = pattern.matcher(sql);
//		return matcher.find();
//	}

	private static boolean contains(String str, List<String[]> inList) {
		boolean exist = false;
		for (String[] record : inList) {
			if (str.toUpperCase().equals(record[0].toUpperCase())) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	private static boolean contains(String str, ArrayList<String> inList) {
		for (String record : inList) {
			if (str.toUpperCase().trim().equals(record.toUpperCase().trim())) {
				return true;
			}
		}
		return false;
	}

}
