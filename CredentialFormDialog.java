
package f.passwordmanager;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class CredentialFormDialog extends JDialog {
    private JTextField titleField = new JTextField(30);
    private JTextField usernameField = new JTextField(30);
    private JPasswordField passwordField = new JPasswordField(30);
    private JTextField urlField = new JTextField(30);
    private JTextArea notesArea = new JTextArea(6, 30);
    private Credential result = null;

    public CredentialFormDialog(JFrame frame, Credential existing) {
        super(frame, true);
        setTitle(existing == null ? "Add Credential" : "Edit Credential");
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Title:"), c);
        c.gridx = 1;
        form.add(titleField, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Username:"), c);
        c.gridx = 1;
        form.add(usernameField, c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Password:"), c);
        c.gridx = 1;
        form.add(passwordField, c);

        // ...
        // ...
        c.gridx = 2; // Add a new grid column
        JCheckBox showCredPass = new JCheckBox("Show");
        showCredPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });
        form.add(showCredPass, c);
        // ...
        // ...

        // Password strength indicator for the dialog
        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Strength:"), c);
        c.gridx = 1;
        JLabel pwdStrength = new JLabel();
        form.add(pwdStrength, c);

        // Attach listener to update strength label
        passwordField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            String pwd = new String(passwordField.getPassword());
            double bits = PasswordUtils.estimateStrengthBits(pwd, false);
            pwdStrength.setText(PasswordUtils.strengthLabel(bits));
        }));

        c.gridx = 0;
        c.gridy = 4;
        form.add(new JLabel("URL:"), c);
        c.gridx = 1;
        form.add(urlField, c);
        c.gridx = 0;
        c.gridy = 5;
        form.add(new JLabel("Notes:"), c);
        c.gridx = 1;
        form.add(new JScrollPane(notesArea), c);

        JPanel buttons = new JPanel();
        JButton gen = new JButton("Generate");
        gen.addActionListener(e -> {
            String pwd = PasswordUtils.generate(16, false, true, true, true);
            passwordField.setText(pwd);
        });
        JButton save = new JButton("Save");
        save.addActionListener(e -> onSave(existing));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            result = null;
            setVisible(false);
        });
        buttons.add(gen);
        buttons.add(save);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(frame);
        if (existing != null) {
            titleField.setText(existing.getTitle());
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            urlField.setText(existing.getUrl());
            notesArea.setText(existing.getNotes());
        }
    }

    private void onSave(Credential existing) {
        String title = titleField.getText().trim();
        String user = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String url = urlField.getText().trim();
        String notes = notesArea.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title required.");
            return;
        }
        if (existing == null) {
            result = new Credential(title, user, pwd, url, notes);
        } else {
            existing.setTitle(title);
            existing.setUsername(user);
            existing.setPassword(pwd);
            existing.setUrl(url);
            existing.setNotes(notes);
            result = existing;
        }
        setVisible(false);
    }

    public Credential getResult() {
        return result;
    }
}