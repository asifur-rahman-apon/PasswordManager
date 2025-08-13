
package f.passwordmanager;

import java.io.Serializable;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String saltBase64;
    private String passwordHash;
    private String vaultFile;
    private String fullName, email, phone;
    private String securityQuestion, securityAnswerHash;
    private int inactivityTimeout;

    public User(String username, String saltBase64, String passwordHash, String vaultFile, String fullName,
            String email, String phone, String securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.saltBase64 = saltBase64;
        this.passwordHash = passwordHash;
        this.vaultFile = vaultFile;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
    }
    // In the User class

    public int getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(int t) {
        this.inactivityTimeout = t;
    }

    // Update the User constructor
    public User(String username, String saltBase64, String passwordHash, String vaultFile, String fullName,
            String email, String phone, String securityQuestion, String securityAnswerHash, int inactivityTimeout) {
        // ... existing assignments ...
        this.inactivityTimeout = inactivityTimeout;
    }

    public String getUsername() {
        return username;
    }

    public String getSaltBase64() {
        return saltBase64;
    }

    public void setSaltBase64(String s) {
        this.saltBase64 = s;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String h) {
        this.passwordHash = h;
    }

    public String getVaultFile() {
        return vaultFile;
    }

    public String getFullName() {
        return fullName == null ? "" : fullName;
    }

    public void setFullName(String n) {
        this.fullName = n;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String e) {
        this.email = e;
    }

    public String getPhone() {
        return phone == null ? "" : phone;
    }

    public void setPhone(String p) {
        this.phone = p;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }
}