package org.onedatashare.server.service;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import org.apache.commons.lang.RandomStringUtils;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.ForbiddenAction;
import org.onedatashare.server.model.error.InvalidField;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<User> createUser(User user) {
    return userRepository.insert(user);
  }

  public Mono<User.UserLogin> login(String email, String password) {
  //  User user = new User("vanditsa@buffalo.edu", "asdasd");
  //  createUser(user).subscribe(System.out::println);
    return getUser(User.normalizeEmail(email))
            .filter(userFromRepository -> userFromRepository.getHash().equals(userFromRepository.hash(password)))
            .map(user1 -> user1.new UserLogin(user1.email, user1.hash))
            .switchIfEmpty(Mono.error(new InvalidField("Invalid username or password")));
  }

  public Object register(String email, String password, String passwordConfirm) {
    User user = new User(email, password);
    if(password.equals(passwordConfirm)) {
      user.setValidationToken(user.validationToken());
      return createUser(user).map(this::sendVerificationEmail);
    }
    else return Mono.error(new InvalidField("Passwords do not match")); //RuntimeException
  }

  public Mono<Boolean> resetPassword(String email, String password, String passwordConfirm, String authToken){
    return getUser(email).flatMap(user-> {
      if(!password.equals(passwordConfirm)){
        return Mono.error(new Exception("Password is not confirmed."));
      }else if(user.getAuthToken() == null){
        return Mono.error(new Exception("Does not have Auth Token"));
      }else if(user.getAuthToken().equals(authToken)){
        user.setPassword(password);
        user.setAuthToken(null);
        userRepository.save(user).subscribe();
        return Mono.just(true);
      }else{
        return Mono.error(new Exception("Wrong Token"));
      }
    });
  }

  public Mono<Boolean> resetPasswordWithOld(String cookie, String oldpassword, String newpassword, String passwordConfirm){
    return getLoggedInUser(cookie).flatMap(user-> {
      if(!newpassword.equals(passwordConfirm)){
        return Mono.error(new Exception("Password is not confirmed."));
      }else if(!user.checkPassword(oldpassword)){
        return Mono.error(new Exception("Old Password is incorrect."));
      }else{
        user.setPassword(newpassword);
        userRepository.save(user).subscribe();
        return Mono.just(true);
      }
    });
  }

  public Object sendVerificationEmail(User user) {
    //TODO
    return null;
  }

  public Mono<User> getUser(String email) {
    return userRepository.findById(email)
            .switchIfEmpty(Mono.error(new Exception("No User found with Id: " + email)));
  }

  public Mono<User> saveUser(User user) {
    return userRepository.save(user);
  }

  public Mono<LinkedList<URI>> saveHistory(String uri, String cookie) {
    return getLoggedInUser(cookie).map(user -> {
      URI historyItem = URI.create(uri);
      if(!user.getHistory().contains(historyItem)) {
        user.getHistory().add(historyItem);
      }
      return user;
    })
    .flatMap(userRepository::save).map(User::getHistory);
  }

  public Mono<LinkedList<URI>> getHistory(String cookie) {
    return getLoggedInUser(cookie).map(User::getHistory);
  }

  public Mono<Boolean> userLoggedIn(String email, String hash) {
    return getUser(email).map(user -> user.getHash().equals(hash))
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new Exception("Invalid login")));
  }

  public Mono<Boolean> sendVerificationCode(String email) {
    // Recipient's email ID needs to be mentioned.
    String to = email;

    final String username = "yifuyin7@gmail.com";
    final String password = "canada332211";

    // Get system properties
    Properties properties = System.getProperties();
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.host", "smtp.gmail.com");
    properties.put("mail.smtp.port", "587");

    // Get the default Session object.
    Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    });


    return getUser(email).flatMap(user -> {
      String code = RandomStringUtils.randomAlphanumeric(6);
      user.setVerifyCode(code);
      userRepository.save(user).subscribe();
      try {
        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(username));
        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // Set Subject: header field
        message.setSubject("Auth Code");
        // Now set the actual message
        message.setText(code);
        // Send message
        Transport.send(message);
        System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
        mex.printStackTrace();
        return Mono.error(new Exception("Email Sending Failed."));
      }
      return Mono.just(true);
    });
  }

  public Mono<Boolean> userLoggedIn(String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    return userLoggedIn(userLogin.email, userLogin.hash);
  }

  public Mono<Boolean> validate(String email, String authToken){
      return getUser(email).flatMap(user-> {
        if(user.validated){
          return Mono.error(new Exception("Already Validated"));
        }else if(user.getAuthToken() == null){
          return Mono.error(new Exception("Did not have Auth Token"));
        }else if(user.getAuthToken().equals(authToken)){
          user.setValidated(true);
          user.setAuthToken(null);
          userRepository.save(user).subscribe();
          return Mono.just(true);
        }else{
          return Mono.error(new Exception("Wrong Token"));
        }
      });
  }

  /**
   * @description verifycode will generation auth token if it is not already created.
   * Every auth token is available to use once.
   * If used auth token is set to null and reset next time user verify the code.
   *
   * @author Yifuyin
   * @param email email of the user of the operation
   * @param code code to verify
   * @return String Auth Token
   */

  public Mono<String> verifyCode(String email, String code){

    return getUser(email).flatMap(user-> {
      User.VerifyCode expectedCode = user.getCode();

//      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//      System.out.println("new date "+dateFormat.format(new Date()));
      if(expectedCode == null){
        return Mono.error(new Exception("code not set"));
      }else if(expectedCode.expireDate.before(new Date())){
        return Mono.error(new Exception("code expired"));
      }else if(expectedCode.code.equals(code)){
        user.setCode(null);
        if(user.getAuthToken() == null){
          user.setAuthToken(code+User.salt(12));
        }
        userRepository.save(user).subscribe();
        return Mono.just(user.authToken);
      }else{
        return Mono.error(new Exception("Code not match"));
      }
    });
  }

  public Mono<Boolean> verifyEmail(String email,String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    if(userLogin.email.equals(email)){
      return Mono.just(true);
    }
    return Mono.error(new Exception("Invalid email"));
  }

  public Mono<User> getLoggedInUser(String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    return userLoggedIn(userLogin.email, userLogin.hash)
      .flatMap(userLoggedIn -> {
        return getUser(userLogin.email);
      });
  }

  public Mono<UUID> saveCredential(String cookie, OAuthCredential credential) {
    final UUID uuid = UUID.randomUUID();
    return  getLoggedInUser(cookie).map(user -> {
              user.getCredentials().put(uuid, credential);
              return user;
            })
            .flatMap(userRepository::save)
            .map(user -> {return uuid;});
  }

  public Mono<Void> deleteCredential(String cookie, String uuid) {
    return getLoggedInUser(cookie)
      .map(user -> {
          if(user.getCredentials().remove(UUID.fromString(uuid))== null) {
            return Mono.error(new NotFound());
          }
        return userRepository.save(user).subscribe();
      }).then();
  }

  public Mono<Void> deleteHistory(String cookie, String uri) {
    return getLoggedInUser(cookie)
      .map(user -> {
        if(user.getHistory().remove(URI.create(uri))) {
          return userRepository.save(user).subscribe();
        }
        return Mono.error(new NotFound());
      }).then();
  }

  public Mono<Object> isAdmin(String cookie){
    return getLoggedInUser(cookie).map(user ->{
       if(user.isAdmin())
         return Mono.just(true);
       else
         return Mono.error(new ForbiddenAction("Invalid Administrator"));
    });
  }


  public Mono<Map<UUID, Credential>> getCredentials(String cookie) {
    return getLoggedInUser(cookie).map(User::getCredentials).map(
            credentials -> removeIfExpired(credentials)).flatMap(creds -> saveCredToUser(creds, cookie));
  }


  public Map<UUID, Credential> removeIfExpired(Map<UUID, Credential> creds){
    ArrayList<UUID> removingThese = new ArrayList<UUID>();
    for(Map.Entry<UUID, Credential> entry : creds.entrySet()){
      if(entry.getValue().type == Credential.CredentialType.OAUTH &&
        ((OAuthCredential)entry.getValue()).expiredTime != null &&
        Calendar.getInstance().getTime().after(((OAuthCredential)entry.getValue()).expiredTime)){
        removingThese.add(entry.getKey());
      }
    }
    for(UUID id : removingThese){
      creds.remove(id);
    }
    return creds;
  }

  public Mono<Map<UUID, Credential>> saveCredToUser(Map<UUID, Credential> creds, String cookie){
    return getLoggedInUser(cookie).map(user -> {
      user.setCredentials(creds);
      return userRepository.save(user);
    }).map(repo -> creds);
  }

  public Flux<UUID> getJobs(String cookie) {
    return getLoggedInUser(cookie).map(User::getJobs).flux().flatMap(Flux::fromIterable);
  }

  public Mono<User> addJob(Job job, String cookie) {
    return getLoggedInUser(cookie).map(user -> user.addJob(job.uuid)).flatMap(userRepository::save);
  }

  public User.UserLogin cookieToUserLogin(String cookie) {
    Map<String,String> map = new HashMap<String,String>();
    Set<Cookie> cookies = CookieDecoder.decode(cookie);
    for (Cookie c : cookies)
      map.put(c.getName(), c.getValue());
    User user = new User();
    user.setEmail(map.get("email"));
    user.setHash(map.get("hash"));
    return user.new UserLogin(user.getEmail(), user.getHash());
  }

}
