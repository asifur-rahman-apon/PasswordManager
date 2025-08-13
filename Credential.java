
package f.passwordmanager;

import java.io.Serializable;
import java.util.UUID;


public class Credential implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private String title, username, password, url, notes;

    public Credential(String title, String username, String password, String url, String notes) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getNotes() {
        return notes;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public void setUrl(String u) {
        this.url = u;
    }

    public void setNotes(String n) {
        this.notes = n;
    }
}