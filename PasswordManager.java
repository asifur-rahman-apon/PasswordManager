

package f.passwordmanager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.swing.*;

import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;


public class PasswordManager {

   private static final File USERS_FILE = new File("users.dat");
    private Storage storage;
    private JFrame frame;
    private User currentUser;
    private SecretKey currentUserKey;

    private CardLayout cardLayout;
    private JPanel rootPanel;

    // login fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // register fields
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regPasswordConfirmField;
    private JTextField regNameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JTextField regSecQField;
    private JPasswordField regSecAnswerField;

    // main
    private DefaultTableModel tableModel;
    private JTable credentialsTable;
    private JTextField searchField;
    private JLabel loggedInLabel;

    // settings
    private JTextField setNameField, setEmailField, setPhoneField;
    private JPasswordField setCurrentPassField, setNewPassField, setNewPassConfirmField;

    // Inside the PasswordManager class
    private Timer sessionTimer;
    private int inactivityTimeoutMinutes = 10; // Default timeout in minutes
    private final long sessionCheckInterval = 1000; // Check every second

    // Add a constant for the timeout preference key, if you want to save it

    private JSpinner timeoutSpinner;

    // Add this inside the PasswordManager class
    private Timer clipboardClearTimer;
    private static final int CLIPBOARD_CLEAR_DELAY_MS = 30000; // 30 seconds

    public PasswordManager() {
        storage = Storage.load(USERS_FILE);
        SwingUtilities.invokeLater(this::buildGui);
    }

    private void buildGui() {
        frame = new JFrame("Enhanced Java Password Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        rootPanel.add(buildWelcomePanel(), "welcome");
        rootPanel.add(buildLoginPanel(), "login");
        rootPanel.add(buildRegisterPanel(), "register");
        rootPanel.add(buildForgotPanel(), "forgot");
        rootPanel.add(buildMainPanel(), "main");
        rootPanel.add(buildSettingsPanel(), "settings");

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Clear the clipboard on application exit
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection emptySelection = new StringSelection("");
                clipboard.setContents(emptySelection, null);
            }
        });
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == KeyEvent.KEY_PRESSED) {
                resetActivityTimer();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        showCard("welcome");
    }

    private JPanel buildWelcomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Welcome to Enhanced Java Password Manager", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        p.add(label, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        loginBtn.addActionListener(e -> showCard("login"));
        registerBtn.addActionListener(e -> showCard("register"));
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Username:"), c);
        c.gridx = 1;
        loginUsernameField = new JTextField(20);
        p.add(loginUsernameField, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(new JLabel("Master Password:"), c);
        c.gridx = 1;
        loginPasswordField = new JPasswordField(20);
        p.add(loginPasswordField, c);

        // ...
        // ...
        c.gridx = 2; // Add a new grid column for the checkbox
        JCheckBox showLoginPass = new JCheckBox("Show");
        showLoginPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loginPasswordField.setEchoChar((char) 0);
            } else {
                loginPasswordField.setEchoChar('*');
            }
        });
        p.add(showLoginPass, c);
        // ...
        // ...
        c.gridx = 0;
        c.gridy = 2;
        JButton back = new JButton("Back");
        back.addActionListener(e -> showCard("welcome"));
        p.add(back, c);
        c.gridx = 1;
        JButton login = new JButton("Login");
        login.addActionListener(e -> doLogin());
        p.add(login, c);
        c.gridx = 1;
        c.gridy = 3;
        JButton forgot = new JButton("Forgot Password");
        forgot.addActionListener(e -> showCard("forgot"));
        p.add(forgot, c);
        return p;
    }

    private JPanel buildRegisterPanel() {

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Choose Username:"), c);
        c.gridx = 1;
        regUsernameField = new JTextField(20);
        p.add(regUsernameField, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(new JLabel("Master Password:"), c);
        c.gridx = 1;
        regPasswordField = new JPasswordField(20);
        p.add(regPasswordField, c);

        // ...
        // ...
        c.gridx = 2; // Add a new grid column
        JCheckBox showRegPass = new JCheckBox("Show");
        showRegPass.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                regPasswordField.setEchoChar((char) 0);
            } else {
                regPasswordField.setEchoChar('*');
            }
        });
        p.add(showRegPass, c);
        // ...
        // ...
        c.gridx = 0;
        c.gridy = 2;
        p.add(new JLabel("Confirm Password:"), c);
        c.gridx = 1;
        regPasswordConfirmField = new JPasswordField(20);
        p.add(regPasswordConfirmField, c);

        // ...
        // ...
        c.gridx = 2; // Add another new grid column
        JCheckBox showRegPassConfirm = new JCheckBox("Show");
        showRegPassConfirm.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                regPasswordConfirmField.setEchoChar((char) 0);
            } else {
                regPasswordConfirmField.setEchoChar('*');
            }
        });
        p.add(showRegPassConfirm, c);
        // ...
        // ...

        // Password strength indicator setup
        c.gridx = 0;
        c.gridy = 3;
        p.add(new JLabel("Password Strength:"), c);
        c.gridx = 1;
        JLabel pwdStrength = new JLabel();
        p.add(pwdStrength, c);

        // Attach listener to update strength label
        regPasswordField.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            String pwd = new String(regPasswordField.getPassword());
            double bits = PasswordUtils.estimateStrengthBits(pwd, false);
            pwdStrength.setText(PasswordUtils.strengthLabel(bits));
        }));

        c.gridx = 0;
        c.gridy = 4;
        p.add(new JLabel("Full Name:"), c);
        c.gridx = 1;
        regNameField = new JTextField(20);
        p.add(regNameField, c);
        c.gridx = 0;
        c.gridy = 5;
        p.add(new JLabel("Email:"), c);
        c.gridx = 1;
        regEmailField = new JTextField(20);
        p.add(regEmailField, c);
        c.gridx = 0;
        c.gridy = 6;
        p.add(new JLabel("Phone:"), c);
        c.gridx = 1;
        regPhoneField = new JTextField(20);
        p.add(regPhoneField, c);

        c.gridx = 0;
        c.gridy = 7;
        p.add(new JLabel("Security Question:"), c);
        // ...
        c.gridx = 1;
        // Replace JComboBox with a JTextField
        regSecQField = new JTextField(20);
        p.add(regSecQField, c);
        c.gridx = 0;
        c.gridy = 8;
        p.add(new JLabel("Security Answer:"), c);
        c.gridx = 1;
        regSecAnswerField = new JPasswordField(20);
        p.add(regSecAnswerField, c);
        // ...

        c.gridx = 0;
        c.gridy = 9;
        JButton back = new JButton("Back");
        back.addActionListener(e -> showCard("welcome"));
        p.add(back, c);
        c.gridx = 1;
        JButton register = new JButton("Register");
        register.addActionListener(e -> doRegister());
        p.add(register, c);

        return p;
    }

    private JPanel buildForgotPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Username:"), c);
        c.gridx = 1;
        JTextField userField = new JTextField(20);
        p.add(userField, c);
        c.gridx = 0;
        c.gridy = 1;
        JButton back = new JButton("Back");
        back.addActionListener(e -> showCard("login"));
        p.add(back, c);
        c.gridx = 1;
        JButton proceed = new JButton("Proceed");
        p.add(proceed, c);

        proceed.addActionListener(e -> {
            String u = userField.getText().trim();
            if (u.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter username");
                return;
            }
            User found = storage.getUser(u);
            if (found == null) {
                JOptionPane.showMessageDialog(frame, "No such user.");
                return;
            }
            String question = found.getSecurityQuestion();
            String ansHash = found.getSecurityAnswerHash();
            String answer = JOptionPane.showInputDialog(frame, question, "Security Question",
                    JOptionPane.QUESTION_MESSAGE);
            if (answer == null)
                return; // cancelled
            try {
                if (PasswordUtils.verifyHash(answer, ansHash)) {
                    int conf = JOptionPane.showConfirmDialog(
                            frame,
                            "Security answer accepted. Resetting master password will ERASE your existing vault "
                                    + "(we cannot recover it without the old password).\n"
                                    + "Proceed and create a NEW master password?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION);
                    if (conf != JOptionPane.YES_OPTION)
                        return;
                    JPanel panel = new JPanel(new GridLayout(3, 2));
                    JPasswordField newp = new JPasswordField();
                    JPasswordField newp2 = new JPasswordField();
                    panel.add(new JLabel("New Master Password:"));
                    panel.add(newp);
                    panel.add(new JLabel("Confirm:"));
                    panel.add(newp2);
                    int ok = JOptionPane.showConfirmDialog(frame, panel, "Set New Password",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (ok != JOptionPane.OK_OPTION)
                        return;
                    String np = new String(newp.getPassword());
                    String np2 = new String(newp2.getPassword());
                    if (np.isEmpty() || !np.equals(np2)) {
                        JOptionPane.showMessageDialog(frame, "Passwords empty or do not match.");
                        return;
                    }

                    // Use the new, more specific password complexity check
                    String complexityError = PasswordUtils.checkComplexity(np);
                    if (complexityError != null) {
                        JOptionPane.showMessageDialog(frame, complexityError);
                        return;
                    }

                    // update user's salt & hash, wipe vault
                    byte[] salt = CryptoUtil.generateSalt();
                    String ph = CryptoUtil.hashPassword(np, salt);
                    found.setSaltBase64(Base64.getEncoder().encodeToString(salt));
                    found.setPasswordHash(ph);
                    storage.save(USERS_FILE);
                    // wipe vault file
                    File vf = new File(found.getVaultFile());
                    if (vf.exists())
                        vf.delete();
                    // create empty encrypted vault with new key
                    SecretKey key = CryptoUtil.deriveKeyFromPassword(np, salt);
                    byte[] enc = CryptoUtil.encryptObject(new ArrayList<Credential>(), key);
                    try (FileOutputStream fos = new FileOutputStream(found.getVaultFile())) {
                        fos.write(enc);
                    }
                    JOptionPane.showMessageDialog(frame,
                            "Password reset complete. You can now login with the new password. Existing vault content was erased.");
                    showCard("login");
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect security answer.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        return p;
    }

    private JPanel buildMainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Credential");
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        // New Copy button
        JButton copyBtn = new JButton("Copy Password");
        JButton settingsBtn = new JButton("Settings");
        JButton logoutBtn = new JButton("Logout");

        addBtn.addActionListener(e -> showAddDialog());
        editBtn.addActionListener(e -> showEditDialog());
        delBtn.addActionListener(e -> deleteSelected());
        settingsBtn.addActionListener(e -> {
            populateSettings();
            showCard("settings");
        });
        logoutBtn.addActionListener(e -> doLogout());

        // New action listener for the Copy button
        copyBtn.addActionListener(e -> {
            int selectedRow = credentialsTable.getSelectedRow();
            if (selectedRow != -1) {
                String password = (String) credentialsTable.getValueAt(selectedRow, 2);
                StringSelection stringSelection = new StringSelection(password);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(frame, "Password copied to clipboard. It will be cleared in 30 seconds.");
                if (clipboardClearTimer != null) {
                    clipboardClearTimer.stop();
                }
                clipboardClearTimer = new Timer(CLIPBOARD_CLEAR_DELAY_MS, event -> {
                    StringSelection emptySelection = new StringSelection("");
                    clipboard.setContents(emptySelection, null);
                    clipboardClearTimer.stop();
                    System.out.println("Clipboard has been cleared.");
                });
                clipboardClearTimer.setRepeats(false);
                clipboardClearTimer.start();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a credential to copy.");
            }
        });

        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(delBtn);
        controls.add(copyBtn); // Add the new button here
        controls.add(settingsBtn);
        controls.add(logoutBtn);

        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refreshTable());
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        top.add(controls, BorderLayout.WEST);
        top.add(searchPanel, BorderLayout.EAST);
        // ...
        tableModel = new DefaultTableModel(new Object[] { "Title", "Username", "Password", "URL", "Notes" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        credentialsTable = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(credentialsTable);
        // ...
        JPanel header = new JPanel(new BorderLayout());
        loggedInLabel = new JLabel("Not logged in");
        loggedInLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        header.add(loggedInLabel, BorderLayout.WEST);
        p.add(header, BorderLayout.NORTH);
        p.add(top, BorderLayout.PAGE_START);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSettingsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Full Name:"), c);
        c.gridx = 1;
        setNameField = new JTextField(20);
        form.add(setNameField, c);
        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Email:"), c);
        c.gridx = 1;
        setEmailField = new JTextField(20);
        form.add(setEmailField, c);
        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Phone:"), c);
        c.gridx = 1;
        setPhoneField = new JTextField(20);
        form.add(setPhoneField, c);

        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("-- Change Master Password --"), c);
        c.gridx = 0;
        c.gridy = 4;
        form.add(new JLabel("Current Password:"), c);
        c.gridx = 1;
        setCurrentPassField = new JPasswordField(20);
        form.add(setCurrentPassField, c);
        c.gridx = 0;
        c.gridy = 5;
        form.add(new JLabel("New Password:"), c);
        c.gridx = 1;
        setNewPassField = new JPasswordField(20);
        form.add(setNewPassField, c);
        // Corrected placement for Confirm New
        c.gridx = 0;
        c.gridy = 6;
        form.add(new JLabel("Confirm New:"), c);
        c.gridx = 1;
        setNewPassConfirmField = new JPasswordField(20);
        form.add(setNewPassConfirmField, c);

        // Auto-logout components
        c.gridx = 0;
        c.gridy = 7;
        form.add(new JLabel("Auto-logout after (minutes):"), c);
        c.gridx = 1;
        SpinnerModel spinnerModel = new SpinnerNumberModel(inactivityTimeoutMinutes, 1, 120, 1);
        timeoutSpinner = new JSpinner(spinnerModel);
        form.add(timeoutSpinner, c);

        JPanel buttons = new JPanel();
        JButton save = new JButton("Save Settings");
        JButton back = new JButton("Back");
        save.addActionListener(e -> saveSettings());
        back.addActionListener(e -> showCard("main"));
        buttons.add(save);
        buttons.add(back);

        p.add(form, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private void showCard(String name) {
        cardLayout.show(rootPanel, name);
    }

    // ----- Actions -----
    private void doRegister() {
        String username = regUsernameField.getText().trim();
        String pass = new String(regPasswordField.getPassword());
        String pass2 = new String(regPasswordConfirmField.getPassword());
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        // ...
        String secQ = regSecQField.getText().trim(); // <-- Get text from the new JTextField
        String secA = new String(regSecAnswerField.getPassword());
        // Add secQ to the validation check
        if (username.isEmpty() || pass.isEmpty() || secQ.isEmpty() || secA.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Username, password, security question and security answer are required.");
            return;
        }
        // ...
        if (!pass.equals(pass2)) {
            JOptionPane.showMessageDialog(frame, "Passwords do not match.");
            return;
        }
        if (storage.userExists(username)) {
            JOptionPane.showMessageDialog(frame, "Username already exists.");
            return;
        }

        // Use the new, more specific password complexity check
        String complexityError = PasswordUtils.checkComplexity(pass);
        if (complexityError != null) {
            JOptionPane.showMessageDialog(frame, complexityError);
            return;
        }

        try {
            byte[] salt = CryptoUtil.generateSalt();
            String ph = CryptoUtil.hashPassword(pass, salt);
            String vaultFile = username + ".vault";
            String secHash = PasswordUtils.hashString(secA);
            User u = new User(username, Base64.getEncoder().encodeToString(salt), ph, vaultFile, name, email, phone,
                    secQ, secHash);
            storage.addUser(u);
            storage.save(USERS_FILE);
            // create empty encrypted vault
            SecretKey key = CryptoUtil.deriveKeyFromPassword(pass, salt);
            byte[] enc = CryptoUtil.encryptObject(new ArrayList<Credential>(), key);
            try (FileOutputStream fos = new FileOutputStream(vaultFile)) {
                fos.write(enc);
            }
            JOptionPane.showMessageDialog(frame,
                    "Registration successful. Please remember your security answer and consider backing up your vault file.");
            // Clear all registration fields after success
            regUsernameField.setText("");
            regPasswordField.setText("");
            regPasswordConfirmField.setText("");
            regNameField.setText("");
            regEmailField.setText("");
            regPhoneField.setText("");
            regSecAnswerField.setText("");
            showCard("login");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());
        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username and password required.");
            return;
        }
        User u = storage.getUser(username);
        if (u == null) {
            JOptionPane.showMessageDialog(frame, "No such user.");
            return;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(u.getSaltBase64());
            boolean ok = CryptoUtil.verifyPassword(pass, salt, u.getPasswordHash());
            if (!ok) {
                JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                return;
            }
            currentUser = u;
            currentUserKey = CryptoUtil.deriveKeyFromPassword(pass, salt);
            loadVault();
            loginUsernameField.setText("");
            loginPasswordField.setText("");
            startSessionTimer(); // Start the timer after successful login
            showCard("main");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Login error: " + ex.getMessage());
        }
    }

    private void doLogout() {
        // Stop the timer when logging out
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        currentUser = null;
        currentUserKey = null;
        tableModel.setRowCount(0);
        showCard("welcome");
    }

    // ----- Vault -----
    private List<Credential> vaultCache = new ArrayList<>();

    private void loadVault() {
        try {
            File vf = new File(currentUser.getVaultFile());
            if (!vf.exists()) {
                vaultCache = new ArrayList<>();
                refreshTable();
                return;
            }
            byte[] enc = readAllBytes(vf);
            Object obj = CryptoUtil.decryptObject(enc, currentUserKey);
            if (obj instanceof List) { // noinspection unchecked
                vaultCache = (List<Credential>) obj;
            } else
                vaultCache = new ArrayList<>();
            loggedInLabel.setText("Logged in: " + currentUser.getUsername());
            refreshTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to load vault: " + ex.getMessage());
            vaultCache = new ArrayList<>();
        }
    }

    private void saveVault() {
        try {
            byte[] enc = CryptoUtil.encryptObject(vaultCache, currentUserKey);
            try (FileOutputStream fos = new FileOutputStream(currentUser.getVaultFile())) {
                fos.write(enc);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to save vault: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String filter = searchField.getText().trim().toLowerCase();
        List<Credential> list = vaultCache;
        if (!filter.isEmpty()) {
            list = list.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(filter)
                            || c.getUsername().toLowerCase().contains(filter)
                            || c.getUrl().toLowerCase().contains(filter))
                    .collect(Collectors.toList());
        }
        for (Credential c : list) {
            tableModel
                    .addRow(new Object[] { c.getTitle(), c.getUsername(), c.getPassword(), c.getUrl(), c.getNotes() });
        }
    }

    private void showAddDialog() {
        CredentialFormDialog d = new CredentialFormDialog(frame, null);
        d.setVisible(true);
        Credential res = d.getResult();
        if (res != null) {
            vaultCache.add(res);
            saveVault();
            refreshTable();
        }
    }

    private void showEditDialog() {
        int idx = credentialsTable.getSelectedRow();
        if (idx < 0) {
            JOptionPane.showMessageDialog(frame, "Select a row to edit.");
            return;
        }
        String filter = searchField.getText().trim().toLowerCase();
        List<Credential> list = vaultCache;
        if (!filter.isEmpty()) {
            list = list.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(filter)
                            || c.getUsername().toLowerCase().contains(filter)
                            || c.getUrl().toLowerCase().contains(filter))
                    .collect(Collectors.toList());
        }
        Credential selected = list.get(idx);
        CredentialFormDialog d = new CredentialFormDialog(frame, selected);
        d.setVisible(true);
        Credential res = d.getResult();
        if (res != null) {
            for (int i = 0; i < vaultCache.size(); i++) {
                if (vaultCache.get(i).getId().equals(selected.getId())) {
                    vaultCache.set(i, res);
                    break;
                }
            }
            saveVault();
            refreshTable();
        }
    }

    private void deleteSelected() {
        int idx = credentialsTable.getSelectedRow();
        if (idx < 0) {
            JOptionPane.showMessageDialog(frame, "Select a row to delete.");
            return;
        }
        String filter = searchField.getText().trim().toLowerCase();
        List<Credential> list = vaultCache;
        if (!filter.isEmpty()) {
            list = list.stream()
                    .filter(c -> c.getTitle().toLowerCase().contains(filter)
                            || c.getUsername().toLowerCase().contains(filter)
                            || c.getUrl().toLowerCase().contains(filter))
                    .collect(Collectors.toList());
        }
        Credential selected = list.get(idx);
        int confirm = JOptionPane.showConfirmDialog(frame, "Delete credential '" + selected.getTitle() + "'?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            vaultCache.removeIf(c -> c.getId().equals(selected.getId()));
            saveVault();
            refreshTable();
        }
    }

    // ----- Settings -----
    private void populateSettings() {
        // Inside populateSettings()
        if (currentUser == null)
            return;
        setNameField.setText(currentUser.getFullName());
        setEmailField.setText(currentUser.getEmail());
        setPhoneField.setText(currentUser.getPhone());
        // Load timeout from a new field in the User object, or use the default
        // For this example, let's assume a simple new field in User.
        timeoutSpinner.setValue(currentUser.getInactivityTimeout()); // You'll need to add this method and field to User
                                                                     // class.
        // Or, if you want to store a global setting for all users
        // You could load from a separate preferences file
        setCurrentPassField.setText("");
        setNewPassField.setText("");
        setNewPassConfirmField.setText("");
    }

    private void saveSettings() {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "No user logged in.");
            return;
        }
        String name = setNameField.getText().trim();
        String email = setEmailField.getText().trim();
        String phone = setPhoneField.getText().trim();
        String cur = new String(setCurrentPassField.getPassword());
        String np = new String(setNewPassField.getPassword());
        String np2 = new String(setNewPassConfirmField.getPassword());
        try {
            // update personal info
            currentUser.setFullName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            // if changing password
            if (!cur.isEmpty() || !np.isEmpty() || !np2.isEmpty()) {
                if (cur.isEmpty() || np.isEmpty() || np2.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "To change password, fill current and new passwords.");
                    return;
                }
                if (!np.equals(np2)) {
                    JOptionPane.showMessageDialog(frame, "New passwords do not match.");
                    return;
                }

                // Use the new, more specific password complexity check
                String complexityError = PasswordUtils.checkComplexity(np);
                if (complexityError != null) {
                    JOptionPane.showMessageDialog(frame, complexityError);
                    return;
                }

                // verify current password
                byte[] salt = Base64.getDecoder().decode(currentUser.getSaltBase64());
                if (!CryptoUtil.verifyPassword(cur, salt, currentUser.getPasswordHash())) {
                    JOptionPane.showMessageDialog(frame, "Current password incorrect.");
                    return;
                }
                // decrypt existing vault with currentUserKey and re-encrypt with new key
                // derived from np
                // derive new salt
                byte[] newSalt = CryptoUtil.generateSalt();
                SecretKey newKey2 = CryptoUtil.deriveKeyFromPassword(np, newSalt);
                // decrypt vault
                byte[] enc = readAllBytes(new File(currentUser.getVaultFile()));
                Object obj = CryptoUtil.decryptObject(enc, currentUserKey);
                List<Credential> data = (obj instanceof List) ? (List<Credential>) obj : new ArrayList<>();
                // update user's salt and password hash
                String newHash = CryptoUtil.hashPassword(np, newSalt);
                currentUser.setSaltBase64(Base64.getEncoder().encodeToString(newSalt));
                currentUser.setPasswordHash(newHash);
                // re-encrypt with newKey2
                byte[] newEnc = CryptoUtil.encryptObject(data, newKey2);
                try (FileOutputStream fos = new FileOutputStream(currentUser.getVaultFile())) {
                    fos.write(newEnc);
                }
                // update in-memory key
                currentUserKey = newKey2;
            }
            storage.save(USERS_FILE);
            JOptionPane.showMessageDialog(frame, "Settings saved.");
            showCard("main");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error saving settings: " + ex.getMessage());
        }
    }

    // ----- Utils -----
    private static byte[] readAllBytes(File f) throws IOException {
        try (FileInputStream fis = new FileInputStream(f); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int r;
            while ((r = fis.read(buf)) != -1)
                baos.write(buf, 0, r);
            return baos.toByteArray();
        }
    }

    private void startSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
        // Convert minutes to milliseconds
        int timeoutMillis = inactivityTimeoutMinutes * 60 * 1000;
        sessionTimer = new Timer((int) sessionCheckInterval, e -> {
            // Check if the time since the last activity exceeds the timeout
            if (System.currentTimeMillis() - lastActivityTime > timeoutMillis) {
                sessionTimer.stop();
                doLogout();
                JOptionPane.showMessageDialog(frame, "You have been logged out due to inactivity.");
            }
        });
        sessionTimer.start();
        resetActivityTimer();
    }

    private long lastActivityTime;

    private void resetActivityTimer() {
        lastActivityTime = System.currentTimeMillis();
    }

    public static void main(String[] args) {
        new PasswordManager();
    }
}


