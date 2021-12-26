package ru.asb.program.bridge.gui;

import ru.asb.program.Program;
import ru.asb.program.bridge.database.MySQLJDBC;
import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.settings.Settings;
import ru.asb.program.bridge.util.CSV;
import ru.asb.program.bridge.util.GuiUtil;
import ru.asb.program.bridge.util.Log;
import ru.asb.program.operation.report.ReportsTables;
import ru.asb.program.operation.report.DataForDB;
import ru.asb.program.operation.report.UniverseTables;
import ru.asb.program.operation.user.UserGetAll;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class Controller {
    private GUI gui;
    private RunPanel runPanel;
    private SettingsPanel settingsPanel;
    private LogPanel logPanel;
    private String currentTab = "Run";

    Controller(GUI gui) {
        this.gui = gui;
        this.runPanel = gui.getRunPanel();
        this.settingsPanel = gui.getSettingsPanel();
        this.logPanel = gui.getLogPanel();
        setActionListener();
    }



    private void setActionListener() {
        //RunController
        runPanel.getRunButton().addActionListener((ActionEvent event) -> {
            try {
                BOConnection.setLogonInfo(
                        settingsPanel.getSapboIpField().getText(),
                        settingsPanel.getSapboLoginField().getText(),
                        String.valueOf(settingsPanel.getSapboPasswordField().getPassword()),
                        settingsPanel.getSapboAuthTypeField().getText()
                );
                Log.setLogFile("logs\\main_" + BOConnection.getHostname() + ".txt");
                switch (runPanel.getCurrentTask()) {
                    case "ReportFindSqlTable" : runReportFindSqlTable();  break;
                    case "UserGetAll" : runReportUserGetAll(); break;
                    case "ReportGetDataForDB" : runReportGetDataForDB(); break;
                    case "UniverseTables" : runUniverseTables(); break;
                }
            } catch(UnknownHostException ex) {
                Log.error("Ошибка в написании IP");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        runPanel.getTaskListBox().addActionListener ((ActionEvent event) -> {
            runPanel.setCurrentTask();
            Log.out("Chosen task " + runPanel.getCurrentTask());
            String task = runPanel.getCurrentTask();
            switch(task) {
                case("ReportFindSqlTable") :
                    runPanel.showFindSqlTableUI(true);
                    runPanel.showDbTablesList(false);
                    break;
                case("UserGetAll") :
                case("ReportGetDataForDB") :
                    runPanel.showFindSqlTableUI(false);
                    runPanel.showDbTablesList(false);
                    runPanel.setDbTableNameListBox(new JComboBox<>(GuiUtil.fillComboBox(Settings.get().getDbTablesList(task))));
                    runPanel.showDbTablesList(true);
                    break;
            }
        });

        runPanel.getStopButton().addActionListener((ActionEvent event)->{
            try {
                if (!gui.getStopwatch().isInterrupted()) {
                    gui.getStopwatch().interrupt();
                }
                if (!runPanel.getRunningThread().isInterrupted()) {
                    runPanel.getRunningThread().interrupt();
                }
                runPanel.getRunButton().setEnabled(true);
            } catch(NullPointerException e) {
                Log.out("No running reports.");
            }
        });

        runPanel.getTable().getModel().addTableModelListener(new TableModelListener() {
            boolean realChange = true;

            @Override
            public void tableChanged(TableModelEvent e) {
                int row = runPanel.getTable().getSelectedRow();
                int column = runPanel.getTable().getSelectedColumn();
                int maxRow = runPanel.getTable().getRowCount();

                if (runPanel.getTable().getRowCount() == (row + 1) && !runPanel.getTable().getModel().getValueAt(row, column).equals("")) {
//                    System.out.println("Row: " + row + " | column: " + column + " | maxRows: " + maxRow + " | cell content: " + runPanel.getTable().getModel().getValueAt(row, column));
                    DefaultTableModel tmodel = (DefaultTableModel) runPanel.getTable().getModel();
                    realChange = false;
                    tmodel.addRow(new Object[] {"*", ""});
                    tmodel.setValueAt(maxRow, maxRow-1, 0);
                    realChange = true;
                }
                if (realChange) {
                    saveTablesFilterList();
                }

            }
        });

        runPanel.getTable().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent event) {
//				 System.out.println(event);
                if (event.getButton() == MouseEvent.BUTTON3) {
                    Point point = event.getPoint();
                    int row = runPanel.getTable().rowAtPoint(point);

                    if (row+1 != runPanel.getTable().getRowCount()) {         //Если строка не последняя - предлагаем удалить строку
                        ContextMenu menu = new ContextMenu(true);
                        menu.show(event.getComponent(), event.getX(), event.getY());

                        menu.getRemoveMenuRow().addActionListener((ActionEvent e) -> {
                            DefaultTableModel tmodel = (DefaultTableModel) runPanel.getTable().getModel();
                            tmodel.removeRow(row);
                            for (int i = 0; i < runPanel.getTable().getRowCount()-1; i++) {	//Обновляем индексы у таблиц
                                tmodel.setValueAt(i+1, i, 0);
                            }
                        });
                    } else {										//Иначе предлагаем вставить строку
                        ContextMenu menu = new ContextMenu(false);
                        menu.show(event.getComponent(), event.getX(), event.getY());
                        menu.getAddMenuRow().addActionListener((ActionEvent e) -> {
                            DefaultTableModel tmodel = (DefaultTableModel) runPanel.getTable().getModel();
                            tmodel.addRow(new Object[] {"*", ""});
                            tmodel.setValueAt(runPanel.getTable().getRowCount()-1, runPanel.getTable().getRowCount()-2, 0);
                        });
                    }
                }
            }
        });


//        runPanel.getSqlCheckBox().addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                System.out.println(runPanel.getSqlCheckBox().isSelected());
//            }
//        });


        //SettingsController
        settingsPanel.getSapboListBox().addActionListener ((ActionEvent event) -> {
            settingsPanel.fillSapboForm();
            String hostname = getServerName(settingsPanel.getSapboIpField().getText().trim());
            runPanel.updateDescription(hostname);
        });

        settingsPanel.getDbListBox().addActionListener ((ActionEvent event) -> {
            settingsPanel.fillDbForm();
        });

        settingsPanel.getSapboIpField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String hostname = getServerName(settingsPanel.getSapboIpField().getText().trim());
                runPanel.updateDescription(hostname);
            }
        });

        addSapboFieldsKeyListener(
                null,
                settingsPanel.getSapboIpField(),
                settingsPanel.getSapboLoginField(),
                settingsPanel.getSapboPasswordField(),
                settingsPanel.getSapboAuthTypeField()
        );

        addDbFieldsKeyListener(
                null,
                settingsPanel.getDbIpField(),
                settingsPanel.getDbPortField(),
                settingsPanel.getDbNameField(),
                settingsPanel.getDbLoginField(),
                settingsPanel.getDbPasswordField()
        );

        settingsPanel.getSapboTestButton().addActionListener((ActionEvent event) -> {
            try {
                BOConnection.setLogonInfo(
                        settingsPanel.getSapboIpField().getText(),
                        settingsPanel.getSapboLoginField().getText(),
                        String.valueOf(settingsPanel.getSapboPasswordField().getPassword()),
                        settingsPanel.getSapboAuthTypeField().getText()
                );
                if (BOConnection.test()) {
                    settingsPanel.getSapboTestLabel().setText("Соединение установлено.");
                    settingsPanel.getSapboTestLabel().setForeground(Color.GREEN);
                } else {
                    settingsPanel.getSapboTestLabel().setText("Аутентификация не пройдена.");
                    settingsPanel.getSapboTestLabel().setForeground(Color.RED);
                }
            } catch(UnknownHostException uhex) {
                settingsPanel.getSapboTestLabel().setText("Ошибка в написании IP");
                settingsPanel.getSapboTestLabel().setForeground(Color.RED);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        });

        settingsPanel.getDbTestButton().addActionListener((ActionEvent event) -> {
            MySQLJDBC.setLogonInfo(
                    settingsPanel.getDbIpField().getText(),
                    settingsPanel.getDbPortField().getText(),
                    settingsPanel.getDbNameField().getText(),
                    settingsPanel.getDbLoginField().getText(),
                    String.valueOf(settingsPanel.getDbPasswordField().getPassword())
            );
            if (MySQLJDBC.test()) {
                settingsPanel.getDbTestLabel().setText("Соединение установлено.");
                settingsPanel.getDbTestLabel().setForeground(Color.GREEN);
            } else {
                settingsPanel.getDbTestLabel().setText("Аутентификация не пройдена.");
                settingsPanel.getDbTestLabel().setForeground(Color.RED);
            }

        });


        //LogController
        logPanel.getOpenButton().addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().edit(new File(Log.getLogFilePath()));	//Можно добавить стандартный file editor
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        });

        //GUIController
        gui.getTabbedPane().addChangeListener((ChangeEvent event) -> {
            if (currentTab.equals("Settings")) {
                Settings.get().save("settings\\SAPBO_connections.param", Settings.get().getSapboConnections());
            }
            currentTab = gui.getTabbedPane().getSelectedComponent().getName();
        });

        gui.getFrame().addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
//                saveTablesFilterList();
                if (runPanel.getRunningThread() != null && runPanel.getRunningThread().isAlive()) {
                    askAndExit();
                } else {
                    Program.exitProgram();
                }
            }
        });

    }

    private int index;
    private void addSapboFieldsKeyListener(JTextField...fields) {
        for (index = 0; index < fields.length; index++) {
            if (fields[index] != null) {
                fields[index].addKeyListener(new KeyAdapter() {
                    int i = index;
                    JTextField currentField = fields[index];
                    @Override
                    public void keyReleased(KeyEvent event) {
                        super.keyReleased(event);
                        String currentConnectionName = settingsPanel.getSapboListBox().getSelectedItem().toString();
                        Settings.get().getSapboConnectionsMap().get(currentConnectionName)[i] = currentField.getText();
                    }
                });
            }
        }
    }

    private void addDbFieldsKeyListener(JTextField...fields) {
        for (index = 0; index < fields.length; index++) {
            if (fields[index] != null) {
                fields[index].addKeyListener(new KeyAdapter() {
                    int i = index;
                    JTextField currentField = fields[index];
                    @Override
                    public void keyReleased(KeyEvent event) {
                        super.keyReleased(event);
                        String currentConnectionName = settingsPanel.getDbListBox().getSelectedItem().toString();
                        Settings.get().getDbConnectionsMap().get(currentConnectionName)[i] = currentField.getText();
                    }
                });
            }
        }
    }

    /**
     * Метод, выполняющий формирование отчета FindSqlTable
     * */
    private void runReportFindSqlTable()  {
        runPanel.getRunButton().setEnabled(false);
        runPanel.getSqlCheckBox().setEnabled(false);
        boolean withSQL = runPanel.getSqlCheckBox().isSelected();
        runPanel.setRunningThread(new ReportsTables(getTablesFilterList(), withSQL));
        runPanel.getRunningThread().start();
    }

    /**
     * Метод, выполняющий формирование отчета UserGetAll
     * */
    private void runReportUserGetAll()  {
        runPanel.getRunButton().setEnabled(false);
        String tableName = String.valueOf(runPanel.getDbTableNameListBox().getSelectedItem());
        runPanel.setRunningThread(new UserGetAll(tableName));
        runPanel.getRunningThread().start();
    }

    /**
     * Метод, выполняющий формирование отчета ReportGetDataForDB
     * */
    private void runReportGetDataForDB() {
        runPanel.getRunButton().setEnabled(false);
        String tableName = String.valueOf(runPanel.getDbTableNameListBox().getSelectedItem());
        runPanel.setRunningThread(new DataForDB(tableName));
        runPanel.getRunningThread().start();
    }

    /**
     * Метод, выполняющий формирование отчета ReportGetDataForDB
     * */
    private void runUniverseTables() {
        runPanel.getRunButton().setEnabled(false);
       // String tableName = String.valueOf(runPanel.getDbTableNameListBox().getSelectedItem());
        runPanel.setRunningThread(new Thread(new UniverseTables()));
        runPanel.getRunningThread().start();
    }

    private List<String> getTablesFilterList() {
        List<String> filterList = new ArrayList<>();
        TableModel tableModel = runPanel.getTable().getModel();
        for (int i = 0; i < tableModel.getRowCount(); i++){
            String tableName = (String) tableModel.getValueAt(i, 1);
            if (!tableName.isEmpty()) filterList.add(tableName.trim().toUpperCase());
        }
        return filterList;
    }


    private void saveTablesFilterList() {
		DefaultTableModel dtm = (DefaultTableModel) runPanel.getTable().getModel();
		String [] tablesList = new String[dtm.getRowCount() - 1];

		for (int i = 0; i < tablesList.length; i++) {
			tablesList[i] = (String) dtm.getValueAt(i, 1);
		}
		try {
			CSV tableFilter = new CSV(tablesList, false);
			tableFilter.toFile("settings\\table_filter.csv").withSeparator(';').withQuoteChar('"').write();
		} catch (Exception e) {
            Log.error("Unable to save filter list to file");
		}
	}

	private String getServerName(String ip) {
        String hostname;
        try {
            InetAddress address = InetAddress.getByName(ip);
            hostname = address.getHostName();
        } catch(UnknownHostException e) {
            hostname = "неизвестное имя хоста " + ip;
        }
        return hostname;
    }

    private void askAndExit() {
        JDialog warn = new JDialog(gui.getFrame(), "Выход из приложения", true);
        warn.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Rectangle fr = gui.getFrame().getBounds();
        warn.setBounds(fr.x + (fr.width/2-150), fr.y + (fr.height/2-75), 300, 150);
        warn.setResizable(false);
        warn.getContentPane().setLayout(null);

        JLabel warnLabel = new JLabel("Прервать текущий процесс");
        warnLabel.setBounds(65, 15, 200, 25);
        warnLabel.setFont(warnLabel.getFont().deriveFont(13.0f));


        JLabel warnLabel_1 = new JLabel("и выйти из приложения?");
        warnLabel_1.setBounds(75, 38, 200, 25);
        warnLabel_1.setFont(warnLabel_1.getFont().deriveFont(13.0f));

        warn.getContentPane().add(warnLabel);
        warn.getContentPane().add(warnLabel_1);

        JButton btnOK = new JButton("OK");
        btnOK.setBounds(50, 78 ,85, 23);
        warn.getContentPane().add(btnOK);

        JButton btnCancel = new JButton("Отмена");
        btnCancel.setBounds(160, 78 ,85, 23);
        warn.getContentPane().add(btnCancel);



        btnOK.addActionListener((ActionEvent event) -> {
            gui.getFrame().setVisible(false);
            Thread.currentThread().interrupt();
//				System.exit(0);
            Program.exitProgram();
        });

        btnCancel.addActionListener((ActionEvent event) -> {
//				warn.setVisible(false);
            warn.dispose();
        });

        warn.setVisible(true);
    }

}
