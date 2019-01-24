package org.onedatashare.server.service;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
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
import java.util.*;

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
//    User user = new User("vanditsa@buffalo.edu", "asdasd");
//    createUser(user).subscribe(System.out::println);
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

  public Mono<Boolean> userLoggedIn(String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    return userLoggedIn(userLogin.email, userLogin.hash);
  }

  public Mono<User> getLoggedInUser(String cookie) {
    final User.UserLogin userLogin = cookieToUserLogin(cookie);
    return userLoggedIn(userLogin.email, userLogin.hash)
            .flatMap(userLoggedIn -> {
              return getUser(userLogin.email);
            });
  }

  public Mono<UUID> saveCredential(String cookie, Mono<OAuthCredential> credential) {
    final UUID uuid = UUID.randomUUID();
    return getLoggedInUser(cookie)
            .flatMap(user -> {
              return credential.map(credential1 -> {
                user.getCredentials().put(uuid, credential1);
                return user;
              });
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
    return getLoggedInUser(cookie).map(User::getCredentials);
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
