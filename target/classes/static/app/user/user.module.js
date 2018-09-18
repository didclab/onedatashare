'use strict';

/** Everything user-related. ngRoute is installed for use of $routeParams*/
angular.module('stork.user', [
  'ngCookies', 'stork','ngRoute'
])

.service('user', function (stork, $location, $rootScope, $cookies) {
  this.user = function () {
    return $rootScope.$user;
  };
  this.login = function (info) {
    if (info) {
      var f = stork.login(info);
      f.then(this.saveLogin, this.forgetLogin);
      return f;
    }
  };
  this.saveLogin = function (u) {
    $rootScope.$user = u;
    $cookies.email = u.email;
    $cookies.hash = u.hash;
  };
  this.forgetLogin = function () {
    delete $rootScope.$user;
    delete $cookies.email;
  };
  this.checkAccess = function (redirectTo) {
    if (!$rootScope.$user)
      $location.path(redirectTo||'/');
  };
  this.setPassword = function (op, np) {
    return stork.postUser({
      action: "password",
      oldPassword: op,
      newPassword: np
    });
  };
  this.passwordReset = function (n, p1) {
    return stork.passwordReset({
      action: "passwordReset",
      authToken: n,
      newPassword: p1
    });
  };

  // If there's a cookie, attempt to log in.
  var u = {
    email: $cookies.email,
    hash:  $cookies.hash
  };
  
  if (u.email && u.hash)
    this.login(u);
  else
    this.forgetLogin();
})

.controller('User', function ($scope, $modal, $location, user, stork, $rootScope) {
  /* If info is given, log the user in. Otherwise show modal. */
  $rootScope.ad = false;
  $scope.changeAdBack = function() {
    $rootScope.ad = false;
    /*Issue 6 changes starts here - Ahmad*/
    //$scope.$apply();
    /*Issue 6 changes ends here - Ahmad*/
  };
  $scope.login = function (info, then) {
    if (!info)
      return $modal({
        title: 'Log in',
        container: 'body',
        contentTemplate: '/app/user/login.html'
      });
    return user.login(info).then(function (v) {
      if (then)
        then(v);
      $modal({
        title: "Welcome!",
        content: "You have successfully logged in.",
        show: true
      });
    }, function (error) {
      $scope.error = error;
    });
  };

  /** ZL: check if a user is a administrator */
  $scope.isAdmin = function (u) {
    return stork.isAdmin(u).then(function(d) {
       $rootScope.ad = true;
       $scope.$apply();
       $location.path('/admin'); 
    },function(e) {
       $modal({
        title: 'Error',
        content: "You are not an administrator. ",
        show: true
       });
    });
  }; 

  /** ZL: for users forgot password, and note that $rootScope can used to pass varibles to the view */ //TODO
  $scope.findPassword = function (u, then) {
    if (!u)
      return $modal({
      title: 'Find your Stork Account',
      container: 'body',
      contentTemplate: '/app/user/beginPasswordReset.html',
    });
    return stork.findPassword(u).then(function (d) {
      if(then)
       then(d); 
      $rootScope.account=u;
      $modal({
        title: 'Send link to reset your password',
        contentTemplate: '/app/user/sendPasswordReset.html',
        show: true
      });      
    },function(e) {
       $modal({
        title: 'We could not find your accout with that information',
        content: e.error,
        show: true
       });
      });
  };
  
  /** ZL: similar to findPassword */
  $scope.findAccount = function (u, then) {
    if (!u)
      return $modal({
      title: 'Find your Stork Account',
      container: 'body',
      contentTemplate: '/app/user/beginResendMail.html',
    });
    return stork.findPassword(u).then(function (d) {
      if(then)
       then(d); 
      $rootScope.account=u;
      $modal({
        title: 'Resend a validation mail',
        contentTemplate: '/app/user/resendMail.html',
        show: true
      });      
    },function(e) {
       $modal({
        title: 'You did not register Stork',
        content: e.error,
        show: true
       });
      });
  };

  /** ZL: add for resend validation mail */
  $scope.sendValidationMail = function (u, then) {
    return stork.sendValidationMail(u).then(function (d) {
      if(then) 
       then(d);
      $modal({
        title: 'Check your email',
        content: "We've sent an email to you to validate your account.",
        show: true
      });
    }, function (e){
        $modal({
          title: 'Error',
          content: e.error,
          show: true
        });
       });
    };

  /** ZL: send email to users that forgot passwords */
  $scope.sendPasswordReset = function (u, then) {
    return stork.sendPasswordReset(u).then(function (d) {
      if(then)
       then(d);
      $modal({
        title: 'Check your email',
        content: "We've sent an email to you. Click the link in the email to reset your password.",
        show: true
      });
    },function(e) {
       $modal({
        title: 'Error',
        content: e.error,
        show: true
       });
      });
  };

  /* Log the user out. */
  $scope.logout = function () {
    user.forgetLogin();
    $location.path('/');
    $modal({
      content: "You have successfully logged out.",
      show: true
    });
  };

  $scope.changePassword = function (op, np1, np2) {
    if (np1 != np2) {
      $modal({
        title: "Mismatched password",
        content: "The passwords do not match.",
        show: true
      });
      return false;
    }

    return user.setPassword(op, np1).then(function (d) {
      $modal({
        title: "Success!",
        content: "Password successfully changed! Please log in again.",
        show: true
      });
      user.forgetLogin();
    }, function (e) {
      $modal({
        title: "Error",
        content: e.error,
        show: true
      });
    });
  };
})

.directive('adminBoolean', function(){
  return{
    
  };
})

/** ZL: for confirm password, not used for now */
.directive('storkPasswordConfirm',function(){
  return{
    require: "ngModel",
    scope: {
        password: "=storkPasswordConfirm"
    },
    link: function(scope, element, attributes, ctrl){
      ctrl.$parsers.unshift(function(modelValue){
        ctrl.$setValidity('storkPasswordConfirm',modelValue == scope.password);
      });
    }
  };
})

/**ZL: get url parameters using $routeParams */
.controller('Account',function($scope, $routeParams, $rootScope, $modal, $location, user, stork){
  $rootScope.authToken = $routeParams.authToken;
  /** ZL: reset password */
  $scope.resetPassword = function (u, p1, p2) {
    if (p1 != p2) {
      $modal({
        title: "Mismatched password",
        content: "The passwords do not match.",
        show: true
      });
      return false;
    }
    return user.passwordReset(u, p1).then(function (d) {
      $location.path('#/');
      $modal({
        title: "Success!",
        content: "Password successfully changed! Please log in again.",
        show: true
      });
      user.forgetLogin();
    }, function (e) {
      $modal({
        title: "Error",
        content: e.error,
        show: true
      });
    });
  };
})

.controller('Admin', function ($scope, $modal, $location, user, stork, $rootScope, $timeout) {
 //TODO: correct this to be real time
 $scope.date = new Date();
 $scope.users = { };
 $scope.administrators = { };
 $scope.auto = true;

 $scope.getUsers = function () {
   return stork.getUsers().then(
      
      function (users) {
         for(var i in users) {
            $scope.users[i] = users[i]; /** get the user list and refer each user to $scope.*/
          /* var u = users[i];
           var i = u.email+' ';
           if(!i) continue;
           if(!$scope.users) $scope.users = {};
           if($scope.users[i])
              angular.extend($scope.users[i], u);
           else $scope.users[i] = u;*/
         }
      }
   );
 };

 $scope.getAdministrators = function () {
   return stork.getAdministrators().then(
      function (administrators) {
        for(var i in administrators) {
           $scope.administrators[i] = administrators[i];
        }
      }
   );
 };

 $scope.autoRefresh = function () {
   if ($scope.auto) {
      $scope.getUsers().then(function () {
         $rootScope.autoTimer = $timeout ($scope.autoRefresh, 1000)
      }, function () {
         $scope.auto = false;
      });
   } 
 };
 
 $scope.color = {
   true:  'progress-bar-success',
   false: 'progress-bar-warning'
 }
   // Pagination
  $scope.perPage = 5;
  $scope.page = 1;

  $scope.pager = function (len) {
    var s = $scope.page-2;
    if (s <= 0)
      s = 1;
    var e = s+5;
    if (e > len/$scope.perPage)
      e = len/$scope.perPage+1;
    return _.range(s, e+1);
  };
  $scope.setPage = function (p) {
    $scope.page = p;
  };

})

.controller('Register', function ($scope, $modal, user, stork) {
/**ZL: if passwords do not match, report error*/
  $scope.register = function (u) {
    if(u.password != u.passwordConfirm) {
      $modal({
        title: "Error",
        content: "Password do not match.",
        show: true
      });
      return false;
    }
    return stork.register(u).then(function (d) {
      $modal({
        title: "Welcome!",
        content: "Thank for you registering with OneDataShare! "+
                 "Please check your email inbox/spam for further instructions.",
        show: true
      });
      delete $scope.user;
    }, function (e) {
      $modal({
        title: "Registration Problem",
        content: e.error,
        show: true
      });
    })
  }
});

