package ru.asb.program.bridge.gui;

import javax.swing.*;

public class Stopwatch extends Thread {
    private JLabel stopWatchLabel;

    Stopwatch(JLabel stopWatchLabel) {
        this.stopWatchLabel = stopWatchLabel;
    }

    @Override
    public void run() {
        try {
            int seconds = 0;
            stopWatchLabel.setText("Время:   0 00:00:00");
            stopWatchLabel.updateUI();

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
                seconds++;
                int minutes = seconds / 60;
                int hours = seconds / 3600;
                int days = seconds / 86400;
                String timer = String.format("Время: %3d %02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60);
                stopWatchLabel.setText(timer);
                stopWatchLabel.updateUI();
            }
        } catch(InterruptedException e) {
        }
    }
}
