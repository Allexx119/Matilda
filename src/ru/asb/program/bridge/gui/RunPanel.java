package ru.asb.program.bridge.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.settings.Settings;
import ru.asb.program.bridge.util.GuiUtil;

public class RunPanel extends JPanel{
	private JButton runButton;
	private JButton stopButton;
	private JPanel taskPanel;
	private JPanel statusPanel;
	private JLabel taskLabel;
	private JComboBox taskListBox;
	private JLabel dbTableNameLabel;
	private JComboBox dbTableNameListBox;
	private JLabel statusLabel;
	private JLabel timeLabel;
	private String currentTask;
	private JLabel description;

	//ReportFindSQLTable
	private JLabel filterLabel;
	private JScrollPane tableScrollPane;
	private STable table;
	private JCheckBox sqlCheckBox;

	private Thread runningThread;
	
	RunPanel() {
		super();
		initialize();
		setCurrentTask();
	}
	
	private void initialize() {
		this.setLayout(null);
		taskPanel = new JPanel();
		taskPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Задание", TitledBorder.LEADING, TitledBorder.TOP, null, Color.GRAY));
		taskPanel.setBounds(10, 11, 759, 420);
		taskPanel.setLayout(null);
		
		statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Статус:", TitledBorder.LEADING, TitledBorder.TOP, null, Color.GRAY));
		statusPanel.setBounds(10, 475, 759, 50);
		statusPanel.setLayout(/*new BoxLayout(statusPanel, BoxLayout.X_AXIS)*/null);

		runButton = new JButton("Run");
		runButton.setBounds(540, 445, 89, 23);
				
		stopButton = new JButton("Stop");
		stopButton.setBounds(655, 445, 89, 23);
				
		tableScrollPane = new JScrollPane();
		tableScrollPane.setBounds(460, 46, 287, 361);
		
		
		try {
			Object [] tHeader = new String [] {"№", "Таблица"};
			table = new STable(new DefaultTableModel(fillTable(Settings.get()), tHeader));
			DefaultTableModel tmodel = (DefaultTableModel) table.getModel();
			tmodel.addRow(new Object[] {"*", ""});
			
			
			table.setRowHeight(20);
			TableColumnModel columnModel = table.getColumnModel();
			columnModel.getColumn(0).setMinWidth(40);
			columnModel.getColumn(0).setMaxWidth(40);
			table.setRowSelectionAllowed(false);
			JTableHeader header = table.getTableHeader();
			
			TableCellRenderer renderer = header.getDefaultRenderer();
			table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
	        	  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	                  JLabel headerLabel = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	               // Выравнивание строки заголовка
	                  headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
	                  return headerLabel;
	              }
	        });
			
	        header.setReorderingAllowed(false);
	        header.setResizingAllowed(false);

	    
		} catch(Exception e) {
			e.printStackTrace();
		}

		taskLabel = new JLabel("Задача:");
		taskLabel.setBounds(10, 26, 46, 14);

		filterLabel = new JLabel("Фильтр по таблицам:");
		filterLabel.setBounds(460, 21, 150, 23);

		description = new JLabel();
		description.setBounds(10, 94, 600, 14);

		sqlCheckBox = new JCheckBox("Добавить SQL запрос в отчет:");
		sqlCheckBox.setHorizontalTextPosition(JCheckBox.LEFT);
		sqlCheckBox.setBounds(6, 60, 180, 14);

		taskListBox = new JComboBox<>(GuiUtil.fillComboBox(Settings.get().getTasks()));
		taskListBox.setBounds(60, 22, 320, 23);
		taskListBox.setMaximumRowCount(5);

		dbTableNameLabel = new JLabel("Имя таблицы в БД:");
		dbTableNameLabel.setBounds(10, 60, 100, 14);

		dbTableNameListBox = new JComboBox<>();

		statusLabel = new JLabel("Готов");
		statusLabel.setBounds(10, 18, 300, 20);

		timeLabel = new JLabel("Время:   0 00:00:00");
		timeLabel.setBounds(646, 18, 100, 20);

		addElementsToPanels();
		updateDescription(BOConnection.getHostname());
	}

	private void addElementsToPanels() {
		this.add(taskPanel);
		this.add(statusPanel);
		this.add(runButton);
		this.add(stopButton);
		
		taskPanel.add(taskLabel);
		taskPanel.add(taskListBox);
		taskPanel.add(filterLabel);
		taskPanel.add(description);

		taskPanel.add(sqlCheckBox);

		taskPanel.add(tableScrollPane);
		tableScrollPane.setViewportView(table);
		
		statusPanel.add(statusLabel);
		statusPanel.add(timeLabel);
	}

	void showDbTablesList(boolean show) {
		if (show) {
			taskPanel.add(dbTableNameLabel);
			taskPanel.add(dbTableNameListBox);
		} else {
			try {
				taskPanel.remove(dbTableNameLabel);
				taskPanel.remove(dbTableNameListBox);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		taskPanel.updateUI();
	}

	void showFindSqlTableUI(boolean show) {
		if (show) {
			taskPanel.add(filterLabel);
			taskPanel.add(tableScrollPane);
			taskPanel.add(sqlCheckBox);
		} else {
			taskPanel.remove(filterLabel);
			taskPanel.remove(tableScrollPane);
			taskPanel.remove(sqlCheckBox);
		}
	}
	
	String getCurrentTask() {
		return currentTask;
	}

	void setCurrentTask() {
		Map<String, String> tasksMap = Settings.get().getTasks();
		String selectedTask = (String) this.taskListBox.getSelectedItem();
		
		for (HashMap.Entry<String, String> pair : tasksMap.entrySet()) {
			if (pair.getValue().equals(selectedTask)) {
				this.currentTask = pair.getKey();
			}
		}
	}
	
	private Object [][] fillTable(Settings settings) {
		Object [] tableList = settings.getTablesFilter().toArray();
		Object [][] tableContent = new Object [tableList.length][2];
		for (int i = 0; i < tableList.length; i++) {
			tableContent[i][0] = i+1;
			tableContent[i][1] = tableList[i];
		}
		return tableContent;
	}


	void setRunningThread(Thread runningThread) {
		this.runningThread = runningThread;
	}

	JComboBox getDbTableNameListBox() {
		return dbTableNameListBox;
	}

	//Панель статуса


	JLabel getStatusLabel() {
		return statusLabel;
	}

	JLabel getTimeLabel() {
		return timeLabel;
	}

	public JButton getRunButton() {
		return runButton;
	}

	JButton getStopButton() {
		return stopButton;
	}

	JPanel getTaskPanel() {
		return taskPanel;
	}

	JPanel getStatusPanel() {
		return statusPanel;
	}

	JLabel getTaskLabel() {
		return taskLabel;
	}

	JLabel getFilterLabel() {
		return filterLabel;
	}

	public JCheckBox getSqlCheckBox() {
		return sqlCheckBox;
	}

	JScrollPane getTableScrollPane() {
		return tableScrollPane;
	}

	STable getTable() {
		return table;
	}

	JComboBox getTaskListBox() {
		return taskListBox;
	}

	public void setStatus(String statusLabel) {
		this.statusLabel.setText(statusLabel);
	}

	void setDbTableNameListBox(JComboBox dbTableNameListBox) {
		this.dbTableNameListBox = dbTableNameListBox;
		this.dbTableNameListBox.setBounds(120, 56, 260, 23);
		this.dbTableNameListBox.setMaximumRowCount(3);
	}

	Thread getRunningThread() {
		return runningThread;
	}

	void updateDescription(String hostname) {
		description.setText("Выгрузка будет выполняться с сервера: " + hostname);
		description.updateUI();
	}


	/**
	 * Класс таблицы с изменяемыми ячейками
	 */
	class STable extends JTable {

		STable(DefaultTableModel dtm) {
			super(dtm);
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex != 0;
		}
	}

}
