
'use strict';

/** The main Stork AngularJS module. Welcome! */
angular.module('stork', [
  'ngRoute', 'ngResource', 'ui', 'cgBusy', 'pasvaz.bindonce',
  'mgcrea.ngStrap', 'mgcrea.ngStrap.collapse', 'mgcrea.ngStrap.tooltip',
  'stork.util', 'stork.user', 'stork.transfer', 'stork.credentials'
  ], function ($provide, $routeProvider) {
    /* This is where you can add routes and change page titles. */
    $routeProvider.when('/', {
      title: 'Home',
      templateUrl: '/app/home/home.html'
    }).when('/transfer', {
      title: 'Transfer',
      templateUrl: '/app/transfer/transfer.html',
      controller: 'Transfer',
      requireLogin: true
    }).when('/queue', {
      title: 'Queue',
      templateUrl: '/app/queue/queue.html',
      controller: 'Transfer',
      requireLogin: true
    }).when('/user', {
      title: 'User Settings',
      controller: 'User',
      templateUrl: '/app/user/user.html',
      requireLogin: true
    }).when('/oauth/:uuid', {
      title: 'OAuth Redirect',
      controller: 'OAuth',
      templateUrl: '/app/credentials/oauth.html',
      requireLogin: true
    }).when('/validate', {
      title: 'Validation',
      templateUrl: '/app/user/validate.html',
    }).when('/validateError', {
      title: 'Error',
      templateUrl: '/app/user/validateError.html',
    }).when('/resetPassword', { //ZL
      title: 'Reset Password',
      controller: 'Account',
      templateUrl: '/app/user/resetPassword.html',
      resolve: {
        identity: function($route, stork){ 
            return stork.getIdentity($route.current.params.authToken);
         }
      }
    }).when('/redirectError',{
      title: 'Link invalid now',
      templateUrl: '/app/user/redirectError.html',
    }).when('/beginPasswordReset',{
      title: 'begin password reset',
      templateUrl: '/app/user/beginPasswordReset.html',
    }).when('/admin',{
      title: 'admin',
      controller: 'Admin',
      templateUrl: '/app/user/admin.html',
    }).when('/userLs',{
      templateUrl: 'app/admin/userLs.html',
    }).when('/terms', {
      title: 'Terms of Service',
      templateUrl: '/app/legal/terms.html'
    }).when('/privacy', {
      title: 'Privacy Policy',
      templateUrl: '/app/legal/privacy.html'
    }).otherwise({
      redirectTo: '/'
    });
  }
)

/** Provides easy access to Stork API resources. */
.factory('stork', function ($window, $http, $q) {
  var gr = function (r) {
    return r.data;
  };
  var ge = function (r) {
    var data = r.data ? r.data : {
      type: "TimeoutException",
      error: "Connection timed out."
    };
    return $q.reject(data);
  };
  return {
    $uri: function (path, query) {
      var uri = new URI('/api/stork/'+path);
      if (typeof query === 'object')
        query = URI.buildQuery(query);
      if (typeof query === 'string')
        uri.query(query);
      return uri.readable();
    },
    $post: function (name, data) {
      return $http({
        method: 'POST',
        url: this.$uri(name),
        data: data,
        timeout: 10000
      }).then(gr, ge);
    },
    $get: function (name, data) {
      return $http({
        method: 'GET',
        url: this.$uri(name, data),
        timeout: 10000
      }).then(gr, ge);
    },
    $download: function (ep) {
      var form = document.createElement('form');
      form.action = this.$uri('get');
      form.method = 'POST';
      form.target = '_blank';

      var input = document.createElement('textarea');
      input.name = '$json';
      input.value = JSON.stringify(ep);
      form.appendChild(input);

      form.style.display = 'none';
      document.body.appendChild(form);
      form.submit();
    },
    login: function (info) {
      return this.$post('user', angular.extend({
        action: 'login'
      }, info));
    },
    register: function (info) {
      return this.$post('user', angular.extend({
        action: 'register'
      }, info));
    },
/** ZL: check if a user is a administrator */
    isAdmin: function (info) {
      return this.$post('user', angular.extend({
        action: 'isAdmin'
      },info));
    }, 
/** ZL: add for find user info */
    findPassword: function (info) {
      return this.$post('user', angular.extend({
         action:'findPassword'
      },info));
    },
/** ZL: add to send password reset email */
    sendPasswordReset: function (info) {
      return this.$post('user', angular.extend({
         action:'sendPasswordReset'
      },info));
    },
/** ZL: add to reset Password */
    passwordReset: function (info) {
      return this.$post('user', info);
    },
/** ZL: get authToken of the user */
    getIdentity: function (info) {
      return this.$post('user', angular.extend({
         action: 'getIdentity'
      }, {authToken: info}));
    },
    getUsers: function () {
      return this.$post('user', angular.extend({
         action:'getUsers'
      }));
    },
/** ZL: get administrators list */
    getAdministrators: function () {
      return this.$post('user', angular.extend({
         action: 'getAdministrators'
      }));
    },
    sendValidationMail: function (info) {
      return this.$post('user',angular.extend({
         action: 'sendValidationMail'
      },info));
    },
    history: function (uri) {
      if (uri) return this.$post('user', {
        action: 'history',
        uri: uri
      });
      return this.$get('user', {action: 'history'});
    },
    getUser: function (info) {
      return this.$post('user', info);
    },
    postUser: function (info) {
      return this.$post('user', info);
    },
    ls: function (ep, d) {
      if (typeof ep === 'string')
        ep = { uri: ep };
      var x = this.$post('ls', angular.extend(angular.copy(ep), {
      depth: d||0
      }));

      return x;
    },
    cancel: function (id) {
      return this.$post('cancel', {
        job_id: id
      });
    },
    q: function (filter, range) {
      return this.$post('q', {
        'status': filter || 'all',
        'range': range
      });
    },
    mkdir: function (ep) {
      if (typeof ep === 'string')
        ep = { uri: ep };
      return this.$post('mkdir', ep);
    },
    delete: function (ep) {
      if (typeof ep === 'string')
        ep = { uri: ep };
      return this.$post('delete', ep);
    },
    submit: function (job) {
      return this.$post('submit', job);
    },
    get: function (uri) {
      this.$download(uri);
    },
    cred: function () {
      return this.$get('cred', {
        action: 'list'
      });
    },
    share: function (ep) {
      return this.$post('share', ep);
    }
  };
})

/*delete...*/
.config(function ($tooltipProvider) {
  /* Configure AngularStrap tooltips. */
  angular.extend($tooltipProvider.defaults, {
    animation: false,
    placement: 'top',
    container: 'body',
    trigger: 'focus hover'
  });
})

.config(function ($locationProvider) {
  $locationProvider.html5Mode(true);
})

.value('cgBusyDefaults',{
    message:'Loading',
    backdrop: false,
    delay: 200
})

.run(function ($location, $document, $rootScope, user) {
  $rootScope.$on('$routeChangeSuccess',
    function (event, current, previous) {
      if (!current.$$route)
        return;
      if (current.$$route.requireLogin)
        user.checkAccess();
      $document[0].title = 'OneDataShare - '+current.$$route.title;
    }
  );
});
