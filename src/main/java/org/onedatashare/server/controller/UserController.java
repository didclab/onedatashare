package org.onedatashare.server.controller;

import org.onedatashare.server.model.error.ForbiddenAction;
import org.onedatashare.server.model.error.InvalidField;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/stork/user")
public class UserController {

  @Autowired
  private UserService userService;

  final int TIMEOUT_IN_MINUTES = 1440;
  private static final String EMAIL_PARAM = "email";
  private static final String HASH_PARAM = "hash";
  @PostMapping
  public Object performAction(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {

    String temp = headers.getFirst("cookie");
    if(temp == null){
      //System.out.println("Email: "+userAction.getEmail()+" Hash: "+userAction.getPassword());
      if(userAction.getEmail()!=null && userAction.getPassword()!=null &&
              !userAction.getEmail().equalsIgnoreCase("") && !userAction.getPassword().equalsIgnoreCase("")){
        temp = EMAIL_PARAM + "=" + userAction.getEmail() + "; " +
                HASH_PARAM + "=" + userAction.getPassword();
      }
    }

    String cookie = temp;

    switch(userAction.action) {
      case "login":
        return userService.login(userAction.email, userAction.password);
      case "register":
        return userService.register(userAction.email, userAction.firstName, userAction.lastName, userAction.organization);
      case "validate":
        return userService.validate(userAction.email, userAction.code);
      case "history":
        return userService.saveHistory(userAction.uri, cookie);
      case "verifyEmail":
        return userService.verifyEmail(userAction.email, headers.getFirst("Cookie"));
      case "sendVerificationCode":
        return userService.sendVerificationCode(userAction.email, TIMEOUT_IN_MINUTES);
      case "getUsers":
        return userService.getAllUsers();
      case "getUser":
        return userService.getUserById(userAction.getEmail());
      case "getAdministrators":
        return userService.getAdministrators();
      case "verifyCode":
        return userService.verifyCode(userAction.email, userAction.code);
      case "setPassword":
        return userService.resetPassword(userAction.email, userAction.password, userAction.confirmPassword,userAction.code);
      case "resetPassword":
        return userService.resetPasswordWithOld(cookie, userAction.password, userAction.newPassword, userAction.confirmPassword);
      case "deleteCredential":
        return userService.deleteCredential(cookie, userAction.uuid);
      case "deleteHistory":
        return userService.deleteHistory(cookie, userAction.uri);
      case "isAdmin":
        return userService.isAdmin(cookie);
      default:
        return null;
    }
  }

  @ExceptionHandler(NotFound.class)
  public ResponseEntity<NotFound> handle(NotFound notfound) {
    return new ResponseEntity<>(notfound, notfound.status);
  }

  @GetMapping
  public Object getHistory(@RequestHeader HttpHeaders headers) {
    return userService.getHistory(headers.getFirst("Cookie"));
  }

  @ExceptionHandler(InvalidField.class)
  public ResponseEntity<InvalidField> handle(InvalidField invf){
    System.out.println(invf.getMessage());
    return new ResponseEntity<>(invf, invf.status);
  }

  @ExceptionHandler(ForbiddenAction.class)
  public ResponseEntity<ForbiddenAction> handle(ForbiddenAction fa){
    System.out.println(fa.getMessage());
    return new ResponseEntity<>(fa, fa.status);
  }
}
