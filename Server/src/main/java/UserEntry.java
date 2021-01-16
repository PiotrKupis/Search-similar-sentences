import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that represents a entry of a single user
 *
 * @author Piotr Kupis
 * @version 1.0, 04 January 2021
 */
@XmlRootElement(name = "UserEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserEntry {

    private String login;
    private String password;

    public UserEntry() {
    }

    public UserEntry(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
