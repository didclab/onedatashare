package org.onedatashare.server.controller;

import org.onedatashare.server.model.error.AuthenticationRequired;
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
@RequestMapping("/api/stork/user")
public class UserController {
  @Autowired
  private UserService userService;

  @PostMapping
  public Object performAction(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {

    switch(userAction.action) {
      case "login":
        return userService.login(userAction.email, userAction.password);
      case "register":
        return userService.register(userAction.email, userAction.password, userAction.confirmPassword);
      case "validate":
        return userService.validate(userAction.email, userAction.code);
      case "history":
        return userService.saveHistory(userAction.uri, headers.getFirst("Cookie"));
      case "verifyEmail":
        return userService.verifyEmail(userAction.email, headers.getFirst("Cookie"));
      case "sendVerificationCode":
        return userService.sendVerificationCode(userAction.email);
      case "verifyCode":
        return userService.verifyCode(userAction.email, userAction.code);
      case "setPassword":
        return userService.resetPassword(userAction.email, userAction.password, userAction.confirmPassword,userAction.newPassword);
      case "resetPassword":
        return userService.resetPasswordWithOld(headers.getFirst("Cookie"), userAction.password, userAction.newPassword, userAction.confirmPassword);
      case "deleteCredential":
        return userService.deleteCredential(headers.getFirst("Cookie"), userAction.uuid);
      case "deleteHistory":
        return userService.deleteHistory(headers.getFirst("Cookie"), userAction.uri);
      case "isAdmin":
        return userService.isAdmin(headers.getFirst("cookie"));
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
