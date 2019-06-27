package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.ForbiddenAction;
import org.onedatashare.server.model.error.InvalidField;
import org.onedatashare.server.model.error.NotFound;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ODSLoggerService;
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

  final int TIMEOUT_IN_MINUTES = 1440;

  @PostMapping
  public Object performAction(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction) {

    String cookie = headers.getFirst(ODSConstants.COOKIE);
    switch(userAction.getAction()) {
      case "login":
        return userService.login(userAction.getEmail(), userAction.getPassword());
      case "register":
        return userService.register(userAction.getEmail(), userAction.getFirstName(), userAction.getLastName(),
                                      userAction.getOrganization());
      case "validate":
        return userService.validate(userAction.getEmail(), userAction.getCode());
      case "history":
        return userService.saveHistory(userAction.getUri(), cookie);
      case "verifyEmail":
        return userService.verifyEmail(userAction.getEmail());
      case "sendVerificationCode":
        return userService.sendVerificationCode(userAction.getEmail(), TIMEOUT_IN_MINUTES);
      case "getUser":
        return userService.getUserFromCookie(userAction.getEmail(), cookie);
      case "getUsers":
        return userService.getAllUsers(userAction, headers.getFirst(ODSConstants.COOKIE));
      case "getAdministrators":
        return userService.getAdministrators(userAction, cookie);
      case "verifyCode":
        return userService.verifyCode(userAction.getEmail(), userAction.getCode());
      case "setPassword":
        return userService.resetPassword(userAction.getEmail(), userAction.getPassword(), userAction.getConfirmPassword(),
                                          userAction.getCode());
      case "resetPassword":
        return userService.resetPasswordWithOld(cookie, userAction.getPassword(), userAction.getNewPassword(),
                                                  userAction.getConfirmPassword());
      case "deleteCredential":
        return userService.deleteCredential(cookie, userAction.getUuid());
      case "deleteHistory":
        return userService.deleteHistory(cookie, userAction.getUri());
      case "isAdmin":
        return userService.isAdmin(cookie);
      case "resendVerificationCode":
        return userService.resendVerificationCode(userAction.getEmail());
      default:
        return null;
    }
  }
  @PutMapping
  public Object putAction(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction){
    switch(userAction.getAction()) {
      case "updateAdminRights":
        return userService.updateAdminRights(userAction.getEmail(), userAction.isAdmin());
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
    return userService.getHistory(headers.getFirst(ODSConstants.COOKIE));
  }

  @ExceptionHandler(InvalidField.class)
  public ResponseEntity<InvalidField> handle(InvalidField invf){
    ODSLoggerService.logError(invf.getMessage());
    return new ResponseEntity<>(invf, invf.status);
  }

  @ExceptionHandler(ForbiddenAction.class)
  public ResponseEntity<ForbiddenAction> handle(ForbiddenAction fa){
    ODSLoggerService.logError(fa.getMessage());
    return new ResponseEntity<>(fa, fa.status);
  }
}
