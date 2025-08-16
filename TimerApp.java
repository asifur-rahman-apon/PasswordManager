package MainApp;
import javax.swing.*;
import java.awt.*;


abstract class AbstractTimer {
    protected boolean running = false;
    protected int seconds = 0;

    public abstract void start();
    public abstract void pause();
    public abstract void reset();
    public int getSeconds() {
        return seconds;
    }
}


class Stopwatch extends AbstractTimer implements Runnable {
    private Thread thread;
    private final JLabel displayLabel;

    public Stopwatch(JLabel displayLabel) {
        this.displayLabel = displayLabel;
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void reset() {
        running = false;
        seconds = 0;
        displayLabel.setText("0");
    }

    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(1000);
                seconds++;
                SwingUtilities.invokeLater(() -> displayLabel.setText(String.valueOf(seconds)));
            }
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Stopwatch interrupted!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


class CountdownTimer extends AbstractTimer implements Runnable {
    private Thread thread;
    private final JLabel displayLabel;
    private final int startValue;

    public CountdownTimer(JLabel displayLabel, int startValue) {
        this.displayLabel = displayLabel;
        this.startValue = startValue;
        this.seconds = startValue;
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void reset() {
        running = false;
        seconds = startValue;
        displayLabel.setText(String.valueOf(startValue));
    }

    @Override
    public void run() {
        try {
            while (running && seconds > 0) {
                Thread.sleep(1000);
                seconds--;
                SwingUtilities.invokeLater(() -> displayLabel.setText(String.valueOf(seconds)));
            }
            if (seconds == 0) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Time's up!"));
            }
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Countdown interrupted!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


class TimerAppGUI extends JFrame {
    private AbstractTimer currentTimer;
    private JLabel displayLabel;
    private JTextField countdownField;
    private final JButton startBtn;
    private final JButton pauseBtn;
    private final JButton resetBtn;
    private final JButton switchModeBtn;
    private boolean stopwatchMode = true;
    private JPanel topPanel; 

    public TimerAppGUI() {
        setTitle("Stopwatch Mode");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

       
        displayLabel = new JLabel("0", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.BOLD, 40));
        add(displayLabel, BorderLayout.CENTER);

       
        topPanel = new JPanel();
        countdownField = new JTextField(5);
        countdownField.setToolTipText("Enter seconds for countdown");
        topPanel.add(new JLabel("Countdown Seconds:"));
        topPanel.add(countdownField);
        add(topPanel, BorderLayout.NORTH);
        topPanel.setVisible(false); 

     
        JPanel btnPanel = new JPanel();
        startBtn = new JButton("Start");
        pauseBtn = new JButton("Pause");
        resetBtn = new JButton("Reset");
        switchModeBtn = new JButton("Switch Mode");

        btnPanel.add(startBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(switchModeBtn);
        add(btnPanel, BorderLayout.SOUTH);

       
        currentTimer = new Stopwatch(displayLabel);

        
        startBtn.addActionListener(e -> {
            if (stopwatchMode) {
                currentTimer.start();
            } else {
                try {
                    int sec = Integer.parseInt(countdownField.getText());
                    if (sec <= 0) throw new NumberFormatException();
                    currentTimer = new CountdownTimer(displayLabel, sec);
                    currentTimer.start();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid positive integer.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pauseBtn.addActionListener(e -> currentTimer.pause());
        resetBtn.addActionListener(e -> currentTimer.reset());

        switchModeBtn.addActionListener(e -> {
            currentTimer.pause();
            currentTimer.reset();
            stopwatchMode = !stopwatchMode;

            if (stopwatchMode) {
                setTitle("Stopwatch Mode");
                topPanel.setVisible(false); 
                currentTimer = new Stopwatch(displayLabel);
            } else {
                setTitle("Countdown Mode");
                topPanel.setVisible(true); 
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}


public class TimerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerAppGUI::new);
    }
}
