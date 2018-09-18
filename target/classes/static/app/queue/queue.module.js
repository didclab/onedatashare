'use strict';

/** Module for monitoring transfers. */
angular.module('stork.transfer.queue', [])

.controller('Queue', function ($scope, $filter, $rootScope, stork, $timeout, $modal) {
  $scope.filters = {
    all: function (j) {
      return true
    },
    pending: function (j) {
      return {
        scheduled: true, processing: true, paused: true
      }[j.status]
    },
    done: function (j) {
      return {
        removed: true, failed: true, complete: true
      }[j.status]
    },
    scheduled:  function (j) { return j.status == 'scheduled' },
    processing: function (j) { return j.status == 'processing' },
    paused:     function (j) { return j.status == 'paused' },
    removed:    function (j) { return j.status == 'removed' },
    failed:     function (j) { return j.status == 'failed' },
    complete:   function (j) { return j.status == 'complete' },
  };
  $scope.filterList = [
    'scheduled', 'processing', 'paused',
    'removed', 'failed', 'complete', null,
    'pending', 'done', 'all'
  ];
  $scope.filter = 'all';

  $scope.jobs = { };

  $scope.auto = true;

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

  $scope.$on('$destroy', function (event) {
    // Clean up the auto-refresh timer.
    if ($rootScope.autoTimer) {
      $timeout.cancel($rootScope.autoTimer);
      delete $rootScope.autoTimer;
    }
  });

  $scope.toggleAuto = function () {
    if ($scope.auto = !$scope.auto)
      $scope.autoRefresh();
  };

  $scope.autoRefresh = function () {
    if ($rootScope.autoTimer) {
      $timeout.cancel($rootScope.autoTimer);
      delete $rootScope.autoTimer;
    } if ($scope.auto) {
      $scope.refresh().then(function () {
        $rootScope.autoTimer = $timeout($scope.autoRefresh, 1000)
      }, function () {
        $scope.auto = false
      });
    }
  };

  $scope.cancel = function (j) {
    $modal({
      contentTemplate: 'cancel-job.html'
    });
    if (j.job_id &&
        confirm("Are you sure you want to remove job "+j.job_id+"?"))
      return stork.cancel(j.job_id).then(
        function (m) {
          j.status = 'removed';
        }, function (e) {
          alert("Failed to remove job: "+e.error);
        }
      );
  };

/* ZL: TODO close window for certain job */
  $scope.showJobs=false;
  $scope.close = function (i, j) {
  if (confirm("Are you sure you want to close job " + j.job_id + "?"))
          //$scope.showJobs=true;
      $scope.jobs.splice($scope.jobs.indexOf(j),1);
  };

  $scope.set_filter = function (f) {
    $scope.filter = f;
  };

  $scope.job_filter = function (j) {
    return j && $scope.filter_set[$scope.filter][j.status];
  }

  $scope.refresh = function () {
    return stork.q().then(
      function (jobs) {
        for (var i in jobs) {
          var j = jobs[i];
          var i = j.job_id+'';
          if (!i)
            continue;
          if (!$scope.jobs)
            $scope.jobs = { };
          if ($scope.jobs[i])
            angular.extend($scope.jobs[i], j);
          else
            $scope.jobs[i] = j;
        }
      }
    );
  };
  $scope.color = {
    processing: 'progress-bar-success progress-striped active',
    scheduled:  'progress-bar-warning',
    complete:   '',
    removed:    'progress-bar-danger',
    failed:     'progress-bar-danger'
  };

  $scope.autoRefresh();
});
