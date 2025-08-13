
package f.passwordmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Storage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, User> users = new HashMap<>();

    public static Storage load(File f) {
        if (!f.exists())
            return new Storage();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object o = ois.readObject();
            if (o instanceof Storage)
                return (Storage) o;
        } catch (Exception ignored) {
        }
        return new Storage();
    }

    public void save(File f) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public void addUser(User u) {
        users.put(u.getUsername().toLowerCase(), u);
    }

    public User getUser(String username) {
        return users.get(username.toLowerCase());
    }
}