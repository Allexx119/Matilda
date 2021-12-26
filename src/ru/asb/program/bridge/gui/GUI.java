package ru.asb.program.bridge.gui;

import ru.asb.program.bridge.settings.Settings;
import ru.asb.program.bridge.util.Log;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import java.awt.Rectangle;
import java.awt.event.*;

public class GUI {
	private static GUI gui = new GUI();
	private JFrame frame;
	private int frameWidth;
	private int frameHeight;

	private JTabbedPane tabbedPane;
	private RunPanel runPanel;
	private LogPanel logPanel;
	private SettingsPanel settingsPanel;
	private Stopwatch stopwatch;

	private boolean init = false;
		
	private GUI() {
		initSystemLookAndFeel();
	}

	public static void runGui() {
		Settings.get().setDefaultConnectionsParameters();
		gui.initialize();
		gui.init = true;
	}

	public static GUI get() {
		return gui;
	}

	private void initSystemLookAndFeel() {
	      try {
	          String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
	          // устанавливаем LookAndFeel
	          UIManager.setLookAndFeel(systemLookAndFeelClassName);
	      } catch (UnsupportedLookAndFeelException e) {
	          System.err.println("Can't use the specified look and feel on this platform.");
	      } catch (Exception e) {
	          System.err.println("Couldn't get specified look and feel, for some reason.");
	      }
	}

	private void initialize() {
		frame = new JFrame("Matilda GUI");
		frame.setBounds(100, 100, 800, 600);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frameWidth = frame.getBounds().width;	//800
		frameHeight = frame.getBounds().height;	//600

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		settingsPanel = new SettingsPanel();
		settingsPanel.setName("Settings");
		runPanel = new RunPanel();
		runPanel.setName("Run");
		logPanel = new LogPanel();
		logPanel.setName("Log");
		new Controller(this);

		tabbedPane.addTab("    Run    ", runPanel);
		tabbedPane.addTab("    Log    ", logPanel);
		tabbedPane.addTab("  Settings  ", settingsPanel);

		Log.setLogTextPane(logPanel.getTextPane());
		
		/**********************************************************************/

		frame.addComponentListener(new ComponentAdapter() {
	        public void componentResized(ComponentEvent evt) {
	    		int dx = frame.getBounds().width - frameWidth;
	    		int dy = frame.getBounds().height - frameHeight;

	    		controlPosition(runPanel.getTaskPanel(), Fix.XY, Fix.NOT, dx, dy);
	    		controlPosition(runPanel.getTaskListBox(),  Fix.XY, Fix.XY, dx, dy);
	    		controlPosition(runPanel.getTable(),  Fix.XY, Fix.X, dx, dy);
	    		controlPosition(runPanel.getRunButton(),  Fix.NOT, Fix.XY, dx, dy);
	    		controlPosition(runPanel.getStopButton(),  Fix.NOT, Fix.XY, dx, dy);
	    		controlPosition(runPanel.getTaskLabel(),  Fix.XY, Fix.XY, dx, dy);
				controlPosition(runPanel.getFilterLabel(),  Fix.Y, Fix.XY, dx, dy);
	    		controlPosition(runPanel.getStatusPanel(), Fix.X, Fix.Y, dx, dy);
	    		controlPosition(runPanel.getStatusLabel(), Fix.XY, Fix.Y, dx, dy);
	    		controlPosition(runPanel.getTimeLabel(), Fix.Y, Fix.XY, dx, dy);
	    		controlPosition(runPanel.getTableScrollPane(), Fix.Y, Fix.X, dx, dy);

	    		controlPosition(logPanel.getScrollPane(),  Fix.XY, Fix.NOT, dx, dy);
	    		controlPosition(logPanel.getOpenButton(),  Fix.NOT, Fix.XY, dx, dy);

	    		frameWidth = frame.getBounds().width;
	    		frameHeight = frame.getBounds().height;
	        }
		});
		
		frame.setVisible(true);
	}
	
	private void controlPosition(JComponent component, Fix position, Fix size, int dx, int dy) {
		Rectangle shape = component.getBounds();
		switch(position) {
		case X: shape.y += dy; break;
		case Y: shape.x += dx; break;
		case XY : break;
		case NOT : shape.y += dy; shape.x += dx; break;
		}
		
		switch(size) {
		case X: shape.height += dy; break;
		case Y: shape.width += dx; break;
		case XY: break;
		case NOT : shape.width += dx; shape.height += dy; break;
		}
		component.setBounds(shape.x, shape.y, shape.width, shape.height);
	}

	JFrame getFrame() {
		return frame;
	}

	JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public RunPanel getRunPanel() {
		return runPanel;
	}

	LogPanel getLogPanel() {
		return logPanel;
	}

	SettingsPanel getSettingsPanel() {
		return settingsPanel;
	}

	public static boolean hasInit() {
		return gui.init;
	}

	public Stopwatch getNewStopwatch() {
		stopwatch = new Stopwatch(runPanel.getTimeLabel());
		return stopwatch;
	}

	public Stopwatch getStopwatch() {
		return stopwatch;
	}
}

