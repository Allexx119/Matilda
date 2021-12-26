package ru.asb.program.bridge.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import ru.asb.program.bridge.util.Helper;
import ru.asb.program.bridge.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static List<String> tables;

    public static void parse(String sqlQuery) throws JSQLParserException {
        tables = new ArrayList<>();
        if (!sqlQuery.isEmpty()) {
            //Нужно добавить подготовку sql запросов
            String sql = Helper.prepareSQL(sqlQuery);
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                tables = tablesNamesFinder.getTableList(selectStatement);
                Log.done("SQL запрос успешно разобран");
            } else {
                Log.info("SQL запрос не является объектом типа Select");
            }
        } else {
            Log.info("Передан пустой SQL запрос. " + sqlQuery);
        }
    }

    public static List<String> getTables() {
        return tables;
    }
}
