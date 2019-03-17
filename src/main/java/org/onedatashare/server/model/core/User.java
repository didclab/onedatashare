package org.onedatashare.server.model.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.util.Util;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.IDN;
import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.onedatashare.module.globusapi.EndPoint;
import java.util.*;

@Data
@Document
public class User {

  /** User's email. */
  @Id
  public String email;
  /** Hashed password. */
  public String hash;
  /** Salt used for hash. */
  public String salt;
  /** User first name. */
  public String firstName;
  /** User last name */
  public String lastName;
  /** User Organization */
  public String organization;
  /** Temp code and expire date **/
  public VerifyCode code;

  /** Set to true once the user has validated registration. */
  public boolean validated = false;

  /** The validation token we're expecting. */
  private String validationToken;

  /** Set to true if user is administrator. */
  public boolean isAdmin = false;
  /** Token for reset password. */
  public String authToken;

  /** Time registered */
  public long registerMoment;

  /** Previously visited URIs. */
  public LinkedList<URI> history = new LinkedList<>();

  /** Previously visited URIs. */
  public Map<UUID,EndPoint> globusEndpoints = new HashMap<>();

  /** Stored credentials. */
  public Map<UUID,Credential> credentials = new HashMap<>();

  /** Job UUIDs with indices corresponding to job IDs. */
  private HashSet<UUID> jobs = new HashSet<>();

  /** Used to hold session connections for reuse. */
  public transient Map<Session,Session> sessions = new HashMap<>();

  /** Basic user login cookie. */
  public static class Cookie {
    public String email;
    public String hash;
    public String password;
    public String authToken;

    protected Cookie() { }

//    /** Attempt to log in with the given information. */
//    public User login() {
//      if (email != null && authToken != null) {
//        User user = server().users.get(User.normalizeEmail(email));
//        return user;
//      }
//      if (email == null || (email = email.trim()).isEmpty())
//        throw new RuntimeException("No email address provided.");
//      if (hash == null && (password == null || password.isEmpty()))
//        throw new RuntimeException("No password provided.");
//      User user = server().users.get(User.normalizeEmail(email));
//      if (user == null)
//        throw new RuntimeException("Invalid username or password.");
//      if (hash == null)
//        hash = user.hash(password);
//      if (!hash.equals(user.hash))
//        throw new RuntimeException("Invalid username or password.");
//      if (!user.validated)
//        throw new RuntimeException("This account has not been validated.");
//      return user;
//    }
  }

  /** The minimum allowed password length. */
  public static final int PASS_LEN = 6;

  /** Create an anonymous user. */
  public User() { }

  /** Create a user with the given email and password. */
  public User(String email, String firstName, String lastName, String organization, String password) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.organization = organization;
    setPassword(password);
  }

  /** Check if the given password is correct for this user. */
  public synchronized boolean checkPassword(String password) {
    return hash(password).equals(hash);
  }

  /** Set the password for this user. Checks password length and hashes. */
  public synchronized void setPassword(String pass) {
    if (pass == null || pass.isEmpty())
      throw new RuntimeException("No password was provided.");
    if (pass.length() < PASS_LEN)
      throw new RuntimeException("Password must be "+PASS_LEN+"+ characters.");
    salt = salt();
    hash = hash(pass);
  }

  /** Get an object containing information to return on login. */
  public Cookie getLoginCookie() {
    Cookie cookie = new Cookie();
    cookie.email = email;
    cookie.hash = hash;
    return cookie;
  }

  public boolean isAdmin(){
    return isAdmin;
  }
  /** Check if a user is anonymous. */
  public boolean isAnonymous() { return email == null; }

  /** Normalize an email string for comparison. */
  public static String normalizeEmail(String email) {
    if (email == null)
      return null;
    String[] parts = email.split("@");
    if (parts.length != 2)
      throw new RuntimeException("Invalid email address.");
    return parts[0].toLowerCase()+"@"+IDN.toASCII(parts[1]).toLowerCase();
  }

  /** Get the normalized email address of this user. */
  public String normalizedEmail() {
    return normalizeEmail(email);
  }

  /** Save a {@link Job} to this {@code User}'s {@code jobs} list. */
  public Job saveJob(Job job) {
    if(job.uuid == null){
      job.uuid();
    }
    job.owner = normalizedEmail();
    jobs.add(job.uuid());
    job.job_id =jobs.size();
    jobs.add(job.uuid);
    return job;
  }

  public User addJob(UUID uuid) {
    jobs.add(uuid);
    return this;
  }

  /** Get one of this user's jobs by its ID. */
//  public synchronized Job getJob(int id) {
//    try {
//      UUID uuid = jobs.get(id);
//      return server().findJob(uuid);
//    } catch (Exception e) {
//      throw new RuntimeException("No job with that ID.", e);
//    }
//  }

//  /** Get a list of actual jobs owned by the user. */
//  public synchronized List<Job> jobs() {
//    // FIXME: Inefficient...
//    List<Job> list = new LinkedList<Job>();
//    for (int i = 0; i < jobs.size(); i++) try {
//      list.add(getJob(i));
//    } catch (Exception e) {
//      // This handles invalid UUIDs in the jobs list.
//    } return list;
//  }

  /** Generate a random salt using a secure random number generator. */
  public static String salt() { return salt(24); }

  /** Generate a random salt using a secure random number generator. */
  public static String salt(int len) {
    byte[] b = new byte[len];
    SecureRandom random = new SecureRandom();
    random.nextBytes(b);
    return Util.formatBytes(b, "%02x");
  }

  /** Hash a password with this user's salt. */
  public String hash(String pass) {
    return hash(pass, salt);
  }

  /** Hash a password with the given salt. */
  public static String hash(String pass, String salt) {
    try {
      String saltpass = salt+'\n'+pass;
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

  /** ZL: TODO: token generator */
  public static String tokenGenerator(String email, String time, String salt) {
    try {
      String saltpass = email+'\n'+time+'\n'+salt;
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = saltpass.getBytes("UTF-8");

      // Run the digest for three rounds.
      for (int i = 0; i < 3; i++)
        digest = md.digest(digest);

      return Util.formatBytes(digest, "%02x");
    } catch (Exception e) {
      throw new RuntimeException("Couldn't generate a token for reset password.");
    }
  }

  /** Add a credential for this user, returning a UUID. */
  public synchronized String addCredential(Credential cred) {
    UUID uuid = UUID.randomUUID();
    credentials.put(uuid, cred);
    return uuid.toString();
  }

  /** Get a simplified list of this user's credentials. */
  public synchronized Map<UUID,Object> credentialList() {
    Map<UUID,Object> map = new HashMap<UUID,Object>();
    for (Map.Entry<UUID,Credential> e : credentials.entrySet())
      map.put(e.getKey(), e.getValue());
    return map;
  }

  /** Validate a user given a token. */
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

  /** Get or create a validation token for this user. */
  public synchronized String validationToken() {
    if (validated)
      throw new RuntimeException("User is already validated.");
    if (validationToken == null)
      validationToken = salt(12);
    return validationToken;
  }

  public synchronized String exist(){
    if (User.this.email == null) throw new RuntimeException("no user exist");
    return User.this.email;
  }
  /**
   * This is thrown when a user is trying to perform an action but is not
   * validated.
   */
  public void setVerifyCode(String code){
    this.code = new VerifyCode(code);
  }
  public void setVerifyCode(String code, int expire_in_minutes){
    VerifyCode _code =  new VerifyCode(code);
    _code.SetExpDate(expire_in_minutes);
    this.code = _code;
  }
  public static class NotValidatedException extends RuntimeException {
    public NotValidatedException() {
      super("This account has not been validated.");
    }
  }

  public class UserLogin {
    public String email;
    public String hash;

    public UserLogin(String email, String hash) {
      this.email = email;
      this.hash = hash;
    }
  }
  @Data
  public class VerifyCode {
    public String code;
    public Date expireDate;
    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    public VerifyCode(String code) {
      this.code = code;
      Calendar date = Calendar.getInstance();
      long t= date.getTimeInMillis();
      this.expireDate = new Date(t + 5 * ONE_MINUTE_IN_MILLIS);
    }

    public void SetExpDate( int minutes) {
      Calendar date = Calendar.getInstance();
      long t= date.getTimeInMillis();
      this.expireDate = new Date(t + minutes * ONE_MINUTE_IN_MILLIS);
    }

  }
}


