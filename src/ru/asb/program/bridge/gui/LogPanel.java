package ru.asb.program.bridge.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import java.awt.*;

class LogPanel extends JPanel{
	private JLabel logLabel;
	private JScrollPane scrollPane;
	private JButton openButton;
	private JTextPane textPane;
	
	LogPanel() {
		super();
		initialize();
	}
	
	private void initialize() {
		this.setLayout(null);
		
		logLabel = new JLabel("Лог выполнения программы:");
		logLabel.setFont(logLabel.getFont().deriveFont(14.0f));
		logLabel.setBounds(10, 11, 250, 23);
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 45, 759, 425);
		
		openButton = new JButton("Открыть лог");
		openButton.setBounds(660, 490, 100, 23);	
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		addElementsToPanels();
	}
	
	private void addElementsToPanels() {
		this.add(logLabel);
		this.add(scrollPane);
		this.add(openButton);
		
		scrollPane.setViewportView(textPane);
	}

	public JButton getOpenButton() {
		return openButton;
	}

	public JTextPane getTextPane() {
		return this.textPane;
	}
	
	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}

	public void setTextPaneText(String text) {
		this.textPane.setText(text);
	}
	
	public void printSize() {
		System.out.println("Log: Width: " + this.getWidth() + " | Height: " + this.getHeight());
	}
}
