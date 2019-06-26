package org.onedatashare.server.model.core;

import lombok.Data;
import org.onedatashare.server.model.util.Util;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.IDN;
import java.net.URI;
import java.security.*;

import org.onedatashare.module.globusapi.EndPoint;

import java.util.*;

@Data
@Document
public class User {

    /** User's email. */
    @Id
    private String email;

    /** Hashed password. */
    private String hash;

    /** Salt used for hash. */
    private String salt;

    /** User first name. */
    private String firstName;

    /** User last name */
    private String lastName;

    /** User last Activity */
    private Long lastActivity;

    /** User Organization */
    private String organization;

    /** Temp code and expire date **/
    private VerifyCode code;

    /** Set to true once the user has validated registration. */
    private boolean validated = false;

    /** The validation token we're expecting. */
    private String validationToken;

    /** Set to true if user is administrator. */
    private boolean isAdmin = false;

    /** Token for reset password. */
    private String authToken;

    /** Registration time */
    private long registerMoment;

    /** Previously visited URIs. */
    private LinkedList<URI> history = new LinkedList<>();

    /** Previously visited URIs. */
    private Map<UUID, EndPoint> globusEndpoints = new HashMap<>();

    /** Stored credentials. */
    private Map<UUID, Credential> credentials = new HashMap<>();

    /** Job UUIDs with indices corresponding to job IDs. */
    private HashSet<UUID> jobs = new HashSet<>();

    /** No. of bits in the KeyPair */
    private static final int keyPairLen = 2048;

    // Key pair for encypting the messages between the end user and the server
    /** RSA public key */
    private String publicKey;
    /** RSA private key */
    private String privateKey;

    /** The minimum allowed password length. */
    public static final int PASS_LEN = 6;

    /** Used to hold session connections for reuse. */
    private transient Map<Session, Session> sessions = new HashMap<>();

    protected static KeyPair getNewRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keyPairLen);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create an anonymous user.
     */
    public User() {
    }

    /**
     * Create a user with the given values.
     */
    public User(String email, String firstName, String lastName, String organization, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.setPassword(password);
        KeyPair keyPair = getNewRSAKeyPair();
        this.publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        this.privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    /**
     * Create a user with the given email and password.
     */
    public User(String email, String password) {
        this.email = email;
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
        return isAdmin;
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
     * Get the normalized email address of this user.
     */
    public String normalizedEmail() {
        return normalizeEmail(email);
    }

    /**
     * Save a {@link Job} to this {@code User}'s {@code jobs} list.
     */
    public Job saveJob(Job job) {
        if (job.getUuid() == null) {
            job.uuid();
        }
        job.setOwner(normalizedEmail());
        job.setJob_id(jobs.size());
        jobs.add(job.getUuid());
        return job;
    }

    /**
     * Link a job to the user
     * @param uuid - Identifier of the job
     * @return Updated user object
     */
    public User addJob(UUID uuid) {
        jobs.add(uuid);
        return this;
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
     * @param code - validation code
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
    public class UserLogin {
        public String email;
        public String hash;
        public String publicKey;


        public UserLogin(String email, String hash, String publicKey) {
            this.email = email;
            this.hash = hash;
            this.publicKey = publicKey;
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


