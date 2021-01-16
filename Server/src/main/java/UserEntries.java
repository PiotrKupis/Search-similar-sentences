import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Class that represents list of UserEntry objects
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
@XmlRootElement(name = "UserEntries")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserEntries {

    @XmlElement(name = "UserEntry")
    private ArrayList<UserEntry> userEntries = null;

    public ArrayList<UserEntry> getUserEntries() {
        return userEntries;
    }

    public void setUserEntries(ArrayList<UserEntry> userEntries) {
        this.userEntries = userEntries;
    }

    /**
     * Method that checks if the list contains the login
     *
     * @param login
     * @return
     */
    public boolean containsLogin(String login) {

        for (UserEntry userEntry : userEntries) {
            if (userEntry.getLogin().equals(login))
                return true;
        }
        return false;
    }

    /**
     * Method that returns password connected to the login
     *
     * @param login
     * @return
     */
    public String getUserPassword(String login) {

        String password = null;

        for (UserEntry userEntry : userEntries) {
            if (userEntry.getLogin().equals(login))
                password = userEntry.getPassword();
        }
        return password;
    }
}
