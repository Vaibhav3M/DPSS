package main.Model;

import java.io.Serializable;

/**
 * The type Player.
 */
public class Player implements Serializable{

    private static final long serialVersionUID = 7526472295622776147L;

    private String firstName;
    private String lastName;
    private int age;

    private String userName;
    private String password;
    private String IPAddress;

    private boolean signedIn;

    @Override
    public String toString() {
        return "Player{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", IPAddress='" + IPAddress + '\'' +
                ", signedIn=" + signedIn +
                '}';
    }


    /**
     * Instantiates a new Player.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @param age       the age
     * @param userName  the user name
     * @param password  the password
     * @param IPAddress the ip address
     * @param signedIn  the signed in
     */
    public Player(String firstName, String lastName, int age, String userName, String password, String IPAddress, Boolean signedIn) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.userName = userName;
        this.password = password;
        this.IPAddress = IPAddress;
        this.signedIn = signedIn;
    }


    /**
     * Gets first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets age.
     *
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets age.
     *
     * @param age the age
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name.
     *
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets ip address.
     *
     * @return the ip address
     */
    public String getIPAddress() {
        return IPAddress;
    }

    /**
     * Sets ip address.
     *
     * @param IPAddress the ip address
     */
    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    /**
     * Is signed in boolean.
     *
     * @return the boolean
     */
    public boolean isSignedIn() {
        return signedIn;
    }

    /**
     * Sets signed in.
     *
     * @param signedIn the signed in
     */
    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

}
