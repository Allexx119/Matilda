package ru.asb.program.bridge.util;

import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;


public class GuiUtil {
	
	public static DefaultComboBoxModel<String> fillComboBox(List<String> list) {
		DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<String>();
		for (String str : list) {
			cbModel.addElement(str);
		}
		return cbModel;
	}
	
	public static DefaultComboBoxModel<String> fillComboBox(Map<String, String> map) {
		DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<String>();
		for (Map.Entry<String, String> pair : map.entrySet()) {
			cbModel.addElement(pair.getValue());
		}
		return cbModel;
	}
}