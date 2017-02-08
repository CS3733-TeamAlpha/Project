package data;


import org.mindrot.jbcrypt.BCrypt;

import static java.sql.DriverManager.println;

public class AdminAccount
{
    /** all references to "attempt" refer to attempted password inputs by the user
     *
     * any method doing pretty much ANYTHING to the data in this class should require that the user
     * input the correct password to see or change information
     *
     */


    private String fname;
    private String lname;
    private String username;
    private String hashword; //it's a pun!

    public AdminAccount()
    {
    }

    public AdminAccount(String fname, String lname, String username, String hashword) {
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.hashword = hashword;
    }

    public String getFname(String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            return fname;
        }
        else {
            println("incorrect password");
            return null;
        }
    }

    public void setFname(String fname, String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            this.fname = fname;
        }
        else {
            println("incorrect password");
        }
    }

    public String getLname(String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            return lname;
        }
        else {
            println("incorrect password");
            return null;
        }
    }

    public void setLname(String lname, String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            this.lname = lname;
        } else {
            println("incorrect password");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username, String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            this.username = username;
        } else {
            println("incorrect password");
        }
    }

    /**
     * the parameter "newPass" is the UNHASHED desired new password for an AdminAccount.
     * the method verifies that the password "attempt", when hashed, matches the existing hashword, as with all other
     * methods within this class, and then replaces the hashword with the hashed output of "newPass"
     */
    public void setHashword(String newPass, String attempt) {
        if (BCrypt.checkpw(attempt, hashword)) {
            this.hashword = BCrypt.hashpw(newPass, BCrypt.gensalt());
        } else {
            println("incorrect password");
        }
    }
}
