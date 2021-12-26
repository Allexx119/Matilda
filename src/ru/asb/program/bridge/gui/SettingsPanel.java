package ru.asb.program.bridge.gui;

import java.awt.Color;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;

import ru.asb.program.bridge.sapbo.BOConnection;
import ru.asb.program.bridge.settings.Settings;
import ru.asb.program.bridge.util.GuiUtil;


class SettingsPanel extends JPanel {
	private JPanel sapboConnectionPanel;
	private JPanel dbConnectionPanel;

	private JLabel titleLabel;

	private JLabel sapboNameLabel;
	private JLabel sapboIpLabel;
	private JLabel sapboLoginLabel;
	private JLabel sapboPasswordLabel;
	private JLabel sapboAuthTypeLabel;
	private JLabel sapboTestLabel;

	private JComboBox sapboListBox;
	private JFormattedTextField sapboIpField;
	private JTextField sapboLoginField;
	private JPasswordField sapboPasswordField;
	private JTextField sapboAuthTypeField;
	private JButton sapboTestButton;

	private JLabel dbLabel;
	private JLabel dbIpLabel;
	private JLabel dbPortLabel;
	private JLabel dbNameLabel;
	private JLabel dbLoginLabel;
	private JLabel dbPasswordLabel;
	private JLabel dbTestLabel;

	private JComboBox dbListBox;
	private JFormattedTextField dbIpField;
	private JFormattedTextField dbPortField;
	private JTextField dbNameField;
	private JTextField dbLoginField;
	private JPasswordField dbPasswordField;
	private JButton dbTestButton;

	SettingsPanel() {
		super();
		this.setBounds(0,0,800,600);
		initialize();
		fillSapboForm();
		fillDbForm();
	}
	
	private void initialize() {
		this.setLayout(null);

		titleLabel = new JLabel("Редактор подключений");
		titleLabel.setBounds(18, 11, 150, 23);
		
		sapboConnectionPanel = new JPanel();
		sapboConnectionPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Выбор подключения к SAP BO", TitledBorder.LEADING, TitledBorder.TOP, null, Color.GRAY));
		sapboConnectionPanel.setBounds(10, 41, 340, 260);
		sapboConnectionPanel.setLayout(null);

		dbConnectionPanel = new JPanel();
		dbConnectionPanel.setBorder(new TitledBorder(new LineBorder(new Color(128, 128, 128), 1, true), "Редактор подключения к БД КАРЕН", TitledBorder.LEADING, TitledBorder.TOP, null, Color.GRAY));
		dbConnectionPanel.setBounds(360, 41, 340, 260);
		dbConnectionPanel.setLayout(null);

		//SAP BO
		sapboNameLabel = new JLabel("Имя подключения:");
		sapboNameLabel.setBounds(20, 20, 150, 23);
		sapboIpLabel = new JLabel("IP-адрес:");
		sapboIpLabel.setBounds(20, 50, 150, 23);
		sapboLoginLabel = new JLabel("Логин:");
		sapboLoginLabel.setBounds(20, 80, 150, 23);
		sapboPasswordLabel = new JLabel("Пароль:");
		sapboPasswordLabel.setBounds(20, 110, 150, 23);
		sapboAuthTypeLabel = new JLabel("Тип входа (опционально):");
		sapboAuthTypeLabel.setBounds(20, 140, 170, 23);

		sapboListBox = new JComboBox<>(GuiUtil.fillComboBox(Settings.get().getConnectionsNames(Settings.get().getSapboConnections())));
		sapboListBox.setBounds(180, 20, 140, 23);
		sapboListBox.setMaximumRowCount(5);
		sapboIpField = new JFormattedTextField();
		sapboIpField.setBounds(180, 50, 140, 23);
		sapboLoginField = new JTextField();
		sapboLoginField.setBounds(180, 80, 140, 23);
		sapboPasswordField = new JPasswordField();
		sapboPasswordField.setBounds(180, 110, 140, 23);
		sapboAuthTypeField = new JTextField();
		sapboAuthTypeField.setBounds(180, 140, 140, 23);

		//Data Base
		dbLabel = new JLabel("База данных:");
		dbLabel.setBounds(20, 20, 150, 23);
		dbIpLabel = new JLabel("IP-адрес:");
		dbIpLabel.setBounds(20, 50, 150, 23);
		dbPortLabel = new JLabel("Порт:");
		dbPortLabel.setBounds(20, 80, 150, 23);
		dbNameLabel = new JLabel("Имя базы данных:");
		dbNameLabel.setBounds(20, 110, 150, 23);
		dbLoginLabel = new JLabel("Логин:");
		dbLoginLabel.setBounds(20, 140, 150, 23);
		dbPasswordLabel = new JLabel("Пароль:");
		dbPasswordLabel.setBounds(20, 170, 150, 23);

		dbListBox = new JComboBox<>(GuiUtil.fillComboBox(Settings.get().getConnectionsNames(Settings.get().getDbConnections())));
		dbListBox.setBounds(180, 20, 140, 23);
		dbListBox.setMaximumRowCount(5);
		dbIpField = new JFormattedTextField();
		dbIpField.setBounds(180, 50, 140, 23);
		dbPortField = new JFormattedTextField();
		dbPortField.setBounds(180, 80, 140, 23);
		dbNameField = new JTextField();
		dbNameField.setBounds(180, 110, 140, 23);
		dbLoginField = new JTextField();
		dbLoginField.setBounds(180, 140, 140, 23);
		dbPasswordField = new JPasswordField();
		dbPasswordField.setBounds(180, 170, 140, 23);

		
		sapboTestButton = new JButton("Тест");
		sapboTestButton.setBounds(207, 217, 89, 23);
		sapboTestLabel = new JLabel("Соединение не установлено");
		sapboTestLabel.setBounds(20, 215, 200, 28);

		dbTestButton = new JButton("Тест");
		dbTestButton.setBounds(207, 217, 89, 23);
		dbTestLabel = new JLabel("Соединение не установлено");
		dbTestLabel.setBounds(20, 215, 200, 28);

		addElementsToPanels();

		for (int i = 0; i < sapboListBox.getItemCount(); i++) {
			if (sapboListBox.getItemAt(i).toString().contains(Settings.get().getSapboConnectionName())) {
				sapboListBox.setSelectedIndex(i);
				break;
			}
		}
		for (int i = 0; i < dbListBox.getItemCount(); i++) {
			if (dbListBox.getItemAt(i).toString().contains(Settings.get().getDbConnectionName())) {
				dbListBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	private void addElementsToPanels() {
		this.add(sapboConnectionPanel);
		this.add(dbConnectionPanel);
		this.add(titleLabel);

		sapboConnectionPanel.add(sapboNameLabel);
		sapboConnectionPanel.add(sapboIpLabel);
		sapboConnectionPanel.add(sapboLoginLabel);
		sapboConnectionPanel.add(sapboPasswordLabel);
		sapboConnectionPanel.add(sapboAuthTypeLabel);
		
		sapboConnectionPanel.add(sapboListBox);
		sapboConnectionPanel.add(sapboIpField);
		sapboConnectionPanel.add(sapboLoginField);
		sapboConnectionPanel.add(sapboPasswordField);
		sapboConnectionPanel.add(sapboAuthTypeField);
		
		sapboConnectionPanel.add(sapboTestButton);
		sapboConnectionPanel.add(sapboTestLabel);


		dbConnectionPanel.add(dbLabel);
		dbConnectionPanel.add(dbIpLabel);
		dbConnectionPanel.add(dbPortLabel);
		dbConnectionPanel.add(dbNameLabel);
		dbConnectionPanel.add(dbLoginLabel);
		dbConnectionPanel.add(dbPasswordLabel);

		dbConnectionPanel.add(dbListBox);
		dbConnectionPanel.add(dbIpField);
		dbConnectionPanel.add(dbPortField);
		dbConnectionPanel.add(dbNameField);
		dbConnectionPanel.add(dbLoginField);
		dbConnectionPanel.add(dbPasswordField);

		dbConnectionPanel.add(dbTestButton);
		dbConnectionPanel.add(dbTestLabel);
	}
	
	void fillSapboForm() {
		String selectedItem = String.valueOf(sapboListBox.getSelectedItem());
		String[] connection = Settings.get().getSapboConnectionsMap().get(selectedItem);
		this.sapboIpField.setText(connection[1]);
		this.sapboLoginField.setText(connection[2]);
		this.sapboPasswordField.setText(connection[3]);
		this.sapboAuthTypeField.setText(connection[4]);
	}

	void fillDbForm() {
		String selectedItem = String.valueOf(dbListBox.getSelectedItem());
		String[] connection = Settings.get().getDbConnectionsMap().get(selectedItem);
		this.dbIpField.setText(connection[1]);
		this.dbPortField.setText(connection[2]);
		this.dbNameField.setText(connection[3]);
		this.dbLoginField.setText(connection[4]);
		this.dbPasswordField.setText(connection[5]);
	}

	JFormattedTextField getSapboIpField() {
		return sapboIpField;
	}

	JTextField getSapboLoginField() {
		return sapboLoginField;
	}

	JPasswordField getSapboPasswordField() {
		return sapboPasswordField;
	}

	JTextField getSapboAuthTypeField() {
		return sapboAuthTypeField;
	}

	JLabel getSapboTestLabel() {
		return sapboTestLabel;
	}

	JLabel getDbTestLabel() {
		return dbTestLabel;
	}

	JComboBox getSapboListBox() {
		return sapboListBox;
	}

	JButton getSapboTestButton() {
		return sapboTestButton;
	}

	JButton getDbTestButton() {
		return dbTestButton;
	}

	JFormattedTextField getDbIpField() {
		return dbIpField;
	}

	JFormattedTextField getDbPortField() {
		return dbPortField;
	}

	JTextField getDbNameField() {
		return dbNameField;
	}

	JTextField getDbLoginField() {
		return dbLoginField;
	}

	JPasswordField getDbPasswordField() {
		return dbPasswordField;
	}

	JComboBox getDbListBox() {
		return dbListBox;
	}

//	public void printSize() {
//		System.out.println("Settings: Width: " + this.getWidth() + " | Height: " + this.getHeight());
//	}
}
