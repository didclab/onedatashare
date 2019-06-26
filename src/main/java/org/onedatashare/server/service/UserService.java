package org.onedatashare.server.service;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.apache.commons.lang.RandomStringUtils;
import org.onedatashare.module.globusapi.EndPoint;
import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.core.UserDetails;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.error.InvalidField;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

/**
 * Service class for all operations related to users' information.
 */
@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EmailService emailService;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Mono<User> createUser(User user) {
    user.setRegisterMoment(System.currentTimeMillis());
    return userRepository.insert(user);
  }

  final int TIMEOUT_IN_MINUTES = 1440;

  public Mono<User.UserLogin> login(String email, String password) {

//    User user = new User("vanditsa@buffalo.edu", "asdasd");
//    createUser(user).subscribe(System.out::println);

    return getUser(User.normalizeEmail(email))
            .filter(userFromRepository -> userFromRepository.getHash().equals(userFromRepository.hash(password)))
            .map(user1 -> user1.new UserLogin(user1.getEmail(), user1.getHash(), user1.getPublicKey()))
            .switchIfEmpty(Mono.error(new InvalidField("Invalid username or password")))
           .doOnSuccess(userLogin -> saveLastActivity(email,System.currentTimeMillis()).subscribe());
  }

  public Object register(String email, String firstName, String lastName, String organization) {

    return doesUserExists(email).flatMap(user -> {

      String password = User.salt(20);
      /*
        This would be a same temporary password for each user while creating,
        once the user goes through the whole User creation workflow, he/she can change the password.
      */
      // Means admin user exists in the DB
      if(user.getEmail() != null && user.getEmail().equals(email)) {
        ODSLoggerService.logWarning("User with email " + email + " already exists.");
        if(!user.isValidated()){
          return sendVerificationCode(email, TIMEOUT_IN_MINUTES);
        }else{
          return Mono.just(new Response("Account already exists",302));
        }
      }
      return createUser(new User(email, firstName, lastName, organization, password))
                  .flatMap(createdUser-> sendVerificationCode(createdUser.getEmail(), TIMEOUT_IN_MINUTES));
    });
  }

  public Mono<User> doesUserExists(String email) {
    User user = new User();
    return userRepository.findById(email)
            .switchIfEmpty(Mono.just(user))
            .onErrorResume(
                    throwable -> throwable instanceof Exception,
                    throwable -> Mono.just(user));
  }

  public GlobusClient getGlobusClientFromUser(User user){
    for (Credential credential : user.getCredentials().values()) {
      if (credential.type == Credential.CredentialType.OAUTH) {
        OAuthCredential oaucr = (OAuthCredential) credential;
        if (oaucr.name.contains("GridFTP")) {
          return new GlobusClient(oaucr.token);
        }
      }
    }
    return new GlobusClient();
  }

  public Mono<GlobusClient> getGlobusClient(String cookie){
    return getLoggedInUser(cookie)
      .map(user -> getGlobusClientFromUser(user));
  }

  public Mono<GlobusClient> getClient(String cookie){
    return getLoggedInUser(cookie)
            .map(user -> {
              for (Credential credential : user.getCredentials().values()) {
                if (credential.type == Credential.CredentialType.OAUTH) {
                  OAuthCredential oaucr = (OAuthCredential) credential;
                  if (oaucr.name.contains("GridFTP")) {
                    return new GlobusClient(oaucr.token);
                  }
                }
              }
              return new GlobusClient();
            });
  }

  public Mono<Boolean> resetPassword(String email, String password, String passwordConfirm, String authToken){
    return getUser(email).flatMap(user-> {
      if(!password.equals(passwordConfirm)){
        return Mono.error(new Exception("Password is not confirmed."));
      }else if(user.getAuthToken() == null){
        return Mono.error(new Exception("Does not have Auth Token"));
      }else if(user.getAuthToken().equals(authToken)){
        user.setPassword(password);
        // Setting the verification code to null while resetting the password.
        // This will allow the user to use the same verification code multiple times with in 24 hrs.
        user.setCode(null);
        user.setAuthToken(null);
        user.setValidated(true);
        userRepository.save(user).subscribe();
        return Mono.just(true);
      }else{
        return Mono.error(new Exception("Wrong Token"));
      }
    });
  }

  public Mono<String> resetPasswordWithOld(String cookie, String oldpassword, String newpassword, String passwordConfirm){
    return getLoggedInUser(cookie).flatMap(user-> {
      if(!newpassword.equals(passwordConfirm)){
        ODSLoggerService.logError("Passwords don't match.");
        return Mono.error(new Exception("Passwords don't match."));
      }else if(!user.checkPassword(oldpassword)){
        ODSLoggerService.logError("Old Password is incorrect.");
        return Mono.error(new Exception("Old Password is incorrect."));
      }else{
        user.setPassword(newpassword);
        userRepository.save(user).subscribe();
        ODSLoggerService.logInfo("Password reset for user " + user.getEmail() + " successful.");
        return Mono.just(user.getHash());
      }
    });
  }

  public Mono<User> getUser(String email) {
    return userRepository.findById(email)
            .switchIfEmpty(Mono.error(new Exception("No User found with Id: " + email)));
  }
  public Mono<User> getUserFromCookie(String email, String cookie) {
    return  getLoggedInUser(cookie).flatMap(user->{
            if(user != null && user.getEmail().equals(email)){
              return Mono.just(user);
            }
            return Mono.error(new Exception("No User found with Id: " + email));
    });
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

  public Mono<Map<UUID,EndPoint>> saveEndpointId(UUID id, EndPoint enp, String cookie) {
    return getLoggedInUser(cookie).map(user -> {
      if(!user.getGlobusEndpoints().containsKey(enp)) {
        user.getGlobusEndpoints().put(id, enp);
      }
      return user;
    }).flatMap(userRepository::save).map(User::getGlobusEndpoints);
  }
  public Mono<Map<UUID,EndPoint>> getEndpointId(String cookie) {
    return getLoggedInUser(cookie).map(User::getGlobusEndpoints);
  }

  public Mono<Void> deleteEndpointId(String cookie, UUID enpid) {
    return getLoggedInUser(cookie)
      .map(user -> {
        if(user.getGlobusEndpoints().remove(enpid) != null) {
          return userRepository.save(user).subscribe();
        }
        return Mono.error(new NotFound());
      }).then();
  }

  public Mono<LinkedList<URI>> getHistory(String cookie) {
    return getLoggedInUser(cookie).map(User::getHistory);
  }

  public Mono<Boolean> userLoggedIn(String email, String hash) {
    return getUser(email).map(user -> user.getHash().equals(hash))
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new Exception("Invalid login")));
  }

  public Mono<Object> resendVerificationCode(String email) {
    return doesUserExists(email).flatMap(user -> {
      if(user.getEmail() == null){
        return Mono.just(new Response("User not registered",500));
      }
      if(!user.isValidated()){
        return sendVerificationCode(email, TIMEOUT_IN_MINUTES);
      }else{
        return Mono.just(new Response("User account is already validated.",500));
      }
    });
  }

  public Mono<Object> sendVerificationCode(String email, int expire_in_minutes) {
    return getUser(email).flatMap(user -> {
      String code = RandomStringUtils.randomAlphanumeric(6);
      user.setVerifyCode(code, expire_in_minutes);
      userRepository.save(user).subscribe();
      try {
        String subject = "OneDataShare Authorization Code";
        String emailText = "The authorization code for your OneDataShare account is : " + code;
        emailService.sendEmail(email, subject, emailText);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return Mono.error(new Exception("Email Sending Failed."));
      }
      return Mono.just(new Response("Success", 200));
    });
  }

  public Mono<UserDetails> getAllUsers(UserAction userAction, String cookie){
    Sort.Direction direction = userAction.getSortOrder().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    return getLoggedInUser(cookie).flatMap(user -> (user != null && user.isAdmin()) ?
       userRepository.findAllBy(PageRequest.of(userAction.getPageNo(),
            userAction.getPageSize(), Sort.by(direction, userAction.getSortBy())))
            .collectList()
            .flatMap(users ->
                userRepository.count()
                    .map(count ->  {
                      UserDetails result = new UserDetails();
                      result.users = users;
                      result.totalCount = count;
                      return result;
                    }))
            : Mono.error(new Exception("The logged in user is not an Admin.")));
  }

  public Mono<UserDetails> getAdministrators(UserAction userAction, String cookie){
    Sort.Direction direction = userAction.getSortOrder().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    return getLoggedInUser(cookie).flatMap(user ->
            (user != null && user.isAdmin()) ?
                    userRepository.findAllAdministrators(PageRequest.of(userAction.getPageNo(),
                        userAction.getPageSize(), Sort.by(direction, userAction.getSortBy())))
                        .collectList()
                        .flatMap(users ->
                              userRepository.countAdministrators()
                                      .map(count ->  {
                                        UserDetails result = new UserDetails();
                                        result.users = users;
                                        result.totalCount = count;
                                        return result;
                                      }))
                    :
                    Mono.error(new Exception("The logged in user is not an Admin.")));
  }

  public Mono<Boolean> updateAdminRights(String email, boolean isAdmin){
    return getUser(email).flatMap(user -> {
      user.setAdmin(isAdmin);
      userRepository.save(user).subscribe();
      return Mono.just(true);
    });
  }

  public Mono<Boolean> userLoggedIn(String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    return userLoggedIn(userLogin.email, userLogin.hash);
  }

  public Mono<Boolean> validate(String email, String authToken){
      return getUser(email).flatMap(user-> {
        if(user.isValidated()){
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
      if(expectedCode == null){
        return Mono.error(new Exception("code not set"));
      }else if(expectedCode.expireDate.before(new Date())){
        return Mono.error(new Exception("code expired"));
      }else if(expectedCode.code.equals(code)){
        user.setAuthToken(code+User.salt(12));
        userRepository.save(user).subscribe();
        return Mono.just(user.getAuthToken());
      }else{
        return Mono.error(new Exception("Code not match"));
      }
    });
  }

  public Mono<Object> verifyEmail(String email) {
    return userRepository.existsById(email).flatMap( bool -> {
      if (bool) {
        return Mono.just(true);
      }else{
        return Mono.error(new Exception("Invalid email"));
     }
    });
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
            .map(user -> uuid);
  }

  public Mono<Void> saveLastActivity(String email, Long lastActivity) {
    return getUser(email).doOnSuccess(user -> {
           user.setLastActivity(lastActivity);
            userRepository.save(user).subscribe();
    }).then();
  }

 public Mono<Long> getLastActivity(String cookie) {
    return getLoggedInUser(cookie).map(user ->user.getLastActivity());
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

  public OAuthCredential updateCredential(String cookie, OAuthCredential credential) {
    //Updating the access token for googledrive using refresh token
          getLoggedInUser(cookie)
            .doOnSuccess(user -> {
                Map<UUID,Credential> credsTemporary = user.getCredentials();
                for(UUID uid : credsTemporary.keySet()){
                  OAuthCredential val = (OAuthCredential) credsTemporary.get(uid);
                  if(val.refreshToken != null && val.refreshToken.equals(credential.refreshToken)){
                    credsTemporary.replace(uid, credential);
                    user.setCredentials(credsTemporary);
                    userRepository.save(user).subscribe();
                  }
                }
            }).subscribe();

    return credential;
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

  public Mono<Boolean> isAdmin(String cookie){
    return getLoggedInUser(cookie).map(user ->user.isAdmin());
  }

  /**
   * Service method that retrieves all existing credentials linked to a user account.
   *
   * @param cookie - Browser cookie string passed in the HTTP request to the controller
   * @return a map containing all the endpoint credentials linked to the user account as a Mono
   */
  public Mono<Map<UUID, Credential>> getCredentials(String cookie) {
    return getLoggedInUser(cookie).map(User::getCredentials).map(
            credentials -> removeIfExpired(credentials)).flatMap(creds -> saveCredToUser(creds, cookie));
  }


  public Map<UUID, Credential> removeIfExpired(Map<UUID, Credential> creds){
    ArrayList<UUID> removingThese = new ArrayList<UUID>();
    for(Map.Entry<UUID, Credential> entry : creds.entrySet()){
      if(entry.getValue().type == Credential.CredentialType.OAUTH &&
              ((OAuthCredential)entry.getValue()).name.equals("GridFTP Client") &&
              ((OAuthCredential)entry.getValue()).expiredTime != null &&
              Calendar.getInstance().getTime().after(((OAuthCredential)entry.getValue()).expiredTime))
      {
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
    }).flatMap(repo -> repo.map(user -> user.getCredentials()));
  }

  public Flux<UUID> getJobs(String cookie) {
    return getLoggedInUser(cookie).map(User::getJobs).flux().flatMap(Flux::fromIterable);
  }

  public Mono<User> addJob(Job job, String cookie) {
    return getLoggedInUser(cookie).map(user -> user.addJob(job.getUuid())).flatMap(userRepository::save);
  }

  public User.UserLogin cookieToUserLogin(String cookie) {
    Map<String,String> map = new HashMap<String,String>();
    Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookie);
    for (Cookie c : cookies)
      map.put(c.name(), c.value());
    User user = new User();
    user.setEmail(map.get("email"));
    user.setHash(map.get("hash"));
    return user.new UserLogin(user.getEmail(), user.getHash(), user.getPublicKey());
  }
}
