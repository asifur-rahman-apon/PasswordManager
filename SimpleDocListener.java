package f.passwordmanager;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleDocListener implements DocumentListener {
    private final Runnable r;

    public SimpleDocListener(Runnable r) {
        this.r = r;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        r.run();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        r.run();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        r.run();
    }
}
