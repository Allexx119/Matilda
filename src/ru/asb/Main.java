package ru.asb;

import org.fusesource.jansi.AnsiConsole;
import ru.asb.program.Program;

public class Main {

	public static void main(String[] args) {
		try {
			AnsiConsole.systemInstall();
			Program	matilda = new Program(args);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Arguments required:");	//Дописать help
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
