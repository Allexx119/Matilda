package ru.asb.program.bridge.util;

import org.fusesource.jansi.Ansi;
import ru.asb.program.bridge.gui.GUI;
import ru.asb.program.bridge.sapbo.BOConnection;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;


import static org.fusesource.jansi.Ansi.ansi;


public class Log {
    private static String logFilePath;
    private static StyledDocument doc;
    private static int rowId = 1;

    static {
        setLogFile("logs\\main.txt");
    }

    public static void out(String message) {
        SimpleAttributeSet textStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(textStyle, Color.BLACK);
        try {
            String formattedMessage = getFormattedMessage(message, "Out");
            if (logFilePath != null) Files.write(Paths.get(logFilePath), formattedMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (doc != null) doc.insertString(doc.getLength(), formattedMessage, textStyle);
            System.out.print(formattedMessage);
        }catch (ClosedByInterruptException inex) {
            System.out.println("Can't to write Log. Thread interrupted by user.");
        } catch(BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void info(String message) {
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.BLUE);
        try {
            String formattedMessage = getFormattedMessage(message, "Info");
            if (logFilePath != null) Files.write(Paths.get(logFilePath), formattedMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (doc != null) doc.insertString(doc.getLength(), formattedMessage, keyWord);
            System.out.print(ansi().fg(Ansi.Color.BLUE).a(formattedMessage).reset());
        }catch (ClosedByInterruptException inex) {
            System.out.println("Can't to write Log. Thread interrupted by user.");
        } catch(BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void error(String message) {
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.RED);
        try {
            String formattedMessage = getFormattedMessage(message, "Error");
            if (logFilePath != null) Files.write(Paths.get(logFilePath), formattedMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (doc != null) doc.insertString(doc.getLength(), formattedMessage, keyWord);

            System.out.print(ansi().fg(Ansi.Color.RED).a(formattedMessage).reset());
        }catch (ClosedByInterruptException inex) {
            System.out.println("Can't to write Log. Thread interrupted by user.");
        } catch(BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void done(String message) {
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.GREEN);
        try {
            String formattedMessage = getFormattedMessage(message, "Done");
            if (logFilePath != null) Files.write(Paths.get(logFilePath), formattedMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (doc != null) doc.insertString(doc.getLength(), formattedMessage, keyWord);
            System.out.print(ansi().fg(Ansi.Color.RED).a(formattedMessage).reset());
        } catch (ClosedByInterruptException inex) {
            System.out.println("Can't to write Log. Thread interrupted by user.");
        } catch(BadLocationException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFormattedMessage(String message, String messageType) {
        String datePattern = "dd.MM.yyyy HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        String date = simpleDateFormat.format(new Date());
        return (rowId++ + "\t(" + messageType + ") " + date + " :\t" + message + System.lineSeparator());
    }

    public static String getLogFilePath() {
        return logFilePath;
    }

    public static void setLogFile(String logFilePath) {
        try {
            Log.logFilePath = logFilePath;
//            Files.deleteIfExists(Paths.get(Log.logFilePath));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String now = System.lineSeparator() + System.lineSeparator() + "--> Log date and time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH));
            Files.write(Paths.get(Log.logFilePath), now.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        }  catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void setLogTextPane(JTextPane logTextPane) {
        Log.doc = logTextPane.getStyledDocument();
    }
}
