'use strict';

/** Module for doing stuff with credentials. */
angular.module('stork.credentials', [])

.controller('Credentials', function ($scope, $modal, stork) {
  $scope.creds = {};
  stork.cred().then(function (creds) {
    $scope.creds = creds;
  });

  /* Open a modal, return a future credential. */
  $scope.newCredential = function (type) {
    return $modal.open({
      templateUrl: 'add-cred-modal.html',
      scope: $scope
    }).result;
  };
})

/** Controller for selecting credentials. */
.controller('SelectCredential', function ($scope) {
  $scope.cred = angular.copy($scope.end.credential);

  if ($scope.cred)
    $scope.selected = $scope.cred.uuid || $scope.cred.type;

  $scope.saveCredential = function (cred) {
    $scope.end.credential = cred;
    $scope.$hide();
    $scope.refresh();
  };

  $scope.changeSelection = function (s) {
    if (!s)
      $scope.cred = undefined;
    else if (s.indexOf("new:") == 0)
      $scope.cred = {type: s.slice(4)};
    else
      $scope.cred = {uuid: s};
  };
})

/** Controller for entering credentials. */
.controller('EnterCredential', function ($scope) {
  $scope.cred = angular.copy($scope.end.credential);

  if ($scope.cred)
    $scope.selected = $scope.cred.uuid || $scope.cred.type;

  $scope.saveCredential = function (cred) {
    $scope.end.credential = cred;
    $scope.$hide();
    $scope.refresh();
  };
})

.controller('OAuth', function ($routeParams, $window) {
  var uuid = $routeParams.uuid;

  // Did someone come here manually? Take them home.
  if (!$window.opener || !$window.opener.oAuthCallback)
    $window.location = '/';

  $window.opener.oAuthCallback(uuid);
  $window.close();
});
