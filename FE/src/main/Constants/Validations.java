package main.Constants;

/**
 * The type Validations.
 */
public class Validations {

    /**
     * Validate user name boolean.
     *
     * @param username the username
     * @return the boolean
     */
    public static boolean validateUserName(String username){

        int userNameLength = username.length();

        return  userNameLength >= 6 && userNameLength <=15;
    }

    /**
     * Validate password boolean.
     *
     * @param password the password
     * @return the boolean
     */
    public static boolean validatePassword(String password){

        return password.length() >= 6;
    }

    /**
     * Validate age boolean.
     *
     * @param age the age
     * @return the boolean
     */
    public static boolean validateAge(int age){

        return age > 0;
    }

    /**
     * Validate ip boolean.
     *
     * @param IP the ip
     * @return the boolean
     */
    public static boolean validateIP(int IP){

        return (IP == 132 || IP == 93 || IP == 182);
    }
}
