/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */


package org.onedatashare.server.model.core;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.onedatashare.server.model.util.Util;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.IDN;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@Document
public class User {

    /**
     * User's email.
     */
    @Id
    private String email;

    /**
     * Hashed password.
     */
    private String hash;

    /**
     * Salt used for hash.
     */
    private String salt;

    /**
     * User first name.
     */
    private String firstName;

    /**
     * User last name
     */
    private String lastName;

    /**
     * User last Activity
     */
    private Long lastActivity;

    /**
     * User Organization
     */
    private String organization;

    /**
     * Temp code and expire date
     **/
    private VerifyCode code;

    /**
     * Set to true once the user has validated registration.
     */
    private boolean validated = false;

    /**
     * The validation token we're expecting.
     */
    private String validationToken;

    /**
     * Set to true if user is administrator. Currently Replaced with roles but still updated
     */
    @Deprecated
    private boolean isAdmin = false;

    /**
     * Set to true if user want to save OAuth credentials
     */
    private boolean saveOAuthTokens = true;

    /**
     * Token for reset password.
     */
    private String authToken;

    /**
     * Registration time
     */
    private long registerMoment;


    /**
     * The minimum allowed password length.
     */
    public static final int PASS_LEN = 6;


    /**
     * Makes sure that view of user in transfer page is consistent with his/her preference.
     */
    private boolean compactViewEnabled = false;

    private List<Role> roles;

    /**
     * Create an anonymous user.
     */
    public User() {
    }

    /**
     * Create a user with the given values.
     */
    public User(String email, String firstName, String lastName, String organization, String password) {
        this.email = normalizeEmail(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.setPassword(password);
        this.roles = new ArrayList<>();
        this.roles.add(Role.USER);
    }

    /**
     * Create a user with the given email and password.
     */
    public User(String email, String password) {
        this.email = normalizeEmail(email);
        this.firstName = "";
        this.lastName = "";
        this.organization = "";
        setPassword(password);
    }

    /**
     * Check if the given password is correct for this user.
     */
    public synchronized boolean checkPassword(String password) {
        return hash(password).equals(hash);
    }

    /**
     * Set the password for this user. Checks password length and hashes.
     */
    public synchronized void setPassword(String pass) {
        if (pass == null || pass.isEmpty())
            throw new RuntimeException("No password was provided.");
        if (pass.length() < PASS_LEN)
            throw new RuntimeException("Password must be " + PASS_LEN + "+ characters.");
        salt = salt();
        hash = hash(pass);
    }

    public boolean isAdmin() {
        return roles != null && (roles.contains(Role.ADMIN) || roles.contains(Role.OWNER));
    }

    /**
     * Check if a user is anonymous.
     */
    public boolean isAnonymous() {
        return email == null;
    }

    /**
     * Normalize an email string for comparison.
     */
    public static String normalizeEmail(String email) {
        if (email == null)
            return null;
        String[] parts = email.split("@");
        if (parts.length != 2)
            throw new RuntimeException("Invalid email address.");
        return parts[0].toLowerCase() + "@" + IDN.toASCII(parts[1]).toLowerCase();
    }


    /**
     * Generate a random salt using a secure random number generator.
     */
    public static String salt() {
        return salt(24);
    }

    /**
     * Generate a random salt using a secure random number generator.
     */
    public static String salt(int len) {
        byte[] b = new byte[len];
        SecureRandom random = new SecureRandom();
        random.nextBytes(b);
        return Util.formatBytes(b, "%02x");
    }

    /**
     * Hash a password with this user's salt.
     */
    public String hash(String pass) {
        return hash(pass, salt);
    }

    /**
     * Hash a password with the given salt.
     */
    public static String hash(String pass, String salt) {
        try {
            String saltpass = salt + '\n' + pass;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = saltpass.getBytes("UTF-8");

            // Run the digest for three rounds.
            for (int i = 0; i < 3; i++)
                digest = md.digest(digest);

            return Util.formatBytes(digest, "%02x");
        } catch (Exception e) {
            throw new RuntimeException("Couldn't hash password.");
        }
    }

    /**
     * Validate a user given a token.
     */
    public synchronized boolean validate(String token) {
        if (validated)
            return true;
        if (token == null || validationToken == null)
            return false;
        if (!token.equals(validationToken))
            return false;
        validationToken = null;
        return validated = true;
    }

    /**
     * This is thrown when a user is trying to perform an action but is not
     * validated.
     */
    public void setVerifyCode(String code) {
        this.code = new VerifyCode(code);
    }

    /**
     * Method that sets the timeout for the validation code based on a constant value
     *
     * @param code              - validation code
     * @param expire_in_minutes - timeout value
     */
    public void setVerifyCode(String code, int expire_in_minutes) {
        VerifyCode _code = new VerifyCode(code);
        _code.SetExpDate(expire_in_minutes);
        this.code = _code;
    }

    /**
     * Model class to hold logged in user specific information
     */
    @Getter
    @Setter
    public class UserLogin {
        public String email;
        public String hash;
        public boolean saveOAuthTokens;
        public boolean compactViewEnabled;


        public UserLogin(String email, String hash, boolean saveOAuthTokens, boolean compactViewEnabled) {
            this.email = email;
            this.hash = hash;
            this.saveOAuthTokens = saveOAuthTokens;
            this.compactViewEnabled = compactViewEnabled;
        }
    }

    /**
     * Model class that holds information for the validation code for the current user account
     */
    public class VerifyCode {
        public String code;
        public Date expireDate;
        static final long ONE_MINUTE_IN_MILLIS = 60000;  //millisecs

        public VerifyCode(String code) {
            this.code = code;
            Calendar date = Calendar.getInstance();
            long t = date.getTimeInMillis();
            this.expireDate = new Date(t + 5 * ONE_MINUTE_IN_MILLIS);
        }

        public void SetExpDate(int minutes) {
            Calendar date = Calendar.getInstance();
            long t = date.getTimeInMillis();
            this.expireDate = new Date(t + minutes * ONE_MINUTE_IN_MILLIS);
        }
    }
}