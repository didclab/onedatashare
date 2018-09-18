'use strict';

/** Module for controlling and monitoring transfers. */
angular.module('stork.transfer', [
  'stork.transfer.browse', 'stork.transfer.queue', 'stork.credentials', 'stork'
])

.factory('fileService', ['stork', '$q', function (stork, $q) {
    return {
      getFiles : function (ep, d) {
        var defer = $q.defer();
        //setTimeout(function (){
          stork.ls(ep, d).then(function (data) {
            defer.resolve(data.hasOwnProperty("files") ? data["files"] : data);
          });
        //}, 0);
        return defer.promise;
      }
    };
  }])

.controller('Transfer', function (
  $rootScope, $q, $timeout, $scope, user, stork, $modal, endpoints, fileService)
{
  // Hardcoded options.
  $scope.optSet = [{
      'title': 'Use transfer optimization',
      'param': 'optimizer',
      'description':
        'Automatically adjust transfer options using the given optimization algorithm.',
      'choices': [ ['None', null], ['2nd Order', '2nd_order'], ['PCP', 'pcp'] ]
    },{
      'title': 'Overwrite existing files',
      'param': 'overwrite',
      'description':
        'By default, destination files with conflicting file names will be overwritten. '+
        'Saying no here will cause the transfer to fail if there are any conflicting files.',
      'choices': [ ['Yes', true], ['No', false] ]
    },{
      'title': 'Verify file integrity',
      'param': 'verify',
      'description':
        'Enable this if you want checksum verification of transferred files.',
      'choices': [ ['Yes', true], ['No', false] ]
    },{
      'title': 'Encrypt data channel',
      'param': 'encrypt',
      'description':
        'Enables data transfer encryption, if supported. This provides additional data security '+
        'at the cost of transfer speed.',
      'choices': [ ['Yes', true], ['No', false] ]
    },{
      'title': 'Compress data channel',
      'param': 'compress',
      'description':
        'Compresses data over the wire. This may improve transfer '+
        'speed if the data is text-based or structured.',
      'choices': [ ['Yes', true], ['No', false] ]
    }
  ];

  $scope.job = {
    src:  endpoints.get('left'),
    dest: endpoints.get('right'),
    options: {
      'optimizer': null,
      'overwrite': true,
      'verify'   : false,
      'encrypt'  : false,
      'compress' : false
    }
  };

  $scope.canTransfer = function (srcName, destName, contents) {
    var src = endpoints.get(srcName);
    var dest = endpoints.get(destName);
    if (!src || !dest || !src.uri || !dest.uri)
      return false;
    if (_.size(src.$selected) < 1 || _.size(dest.$selected) != 1)
      return false;
    if (!_.values(dest.$selected)[0].dir)
      return false;
    if (_.values(src.$selected)[0].dir && !_.values(dest.$selected)[0].dir)
      /*$modal({
      title: 'ATTENTION',
      contentTemplate: 'transfer-error.html',
      });*/
      return false;
    if(!$scope.flag)
      return false;
    return true;
  };


  function getSourceFiles(src){
    var res = [];
    var i = 0;
    for(var key in src){
      if(src.hasOwnProperty(key)){
        res[i++] = src[key];
      }
    }
    return res;
  }

  function loadDestFiles(dest, key, srcFiles, src){
    var ep = angular.copy(dest);
    ep.uri = key;
    fileService.getFiles(ep, 1).then(function (data){
      getDuplicates(data, srcFiles, src, dest);
    });
  }

  function getDestDirFiles(dest, srcFiles, src){
    var destSelected = dest.$selected;
    for (var key in destSelected) {
      if (destSelected.hasOwnProperty(key)) {
        if (destSelected[key].hasOwnProperty("files"))
          getDuplicates(destSelected[key]["files"], srcFiles, src, dest);
        else {
          loadDestFiles(dest, key, srcFiles, src);
          //return destFiles;
        }
      }
    }
  }

  function getDestDirFileNames(destDirFiles, isFile) {
    var names = [];
    var n = 0;
    for(var i = 0, len = destDirFiles.length; i < len; i++){
      if(destDirFiles[i]["file"] === isFile)
        names[n++] = destDirFiles[i]["name"];
    }
    return names;
  }

  function getDuplicates(destDirFiles, srcFiles, src, dest){
    var d = 0;
    if(destDirFiles != undefined){
      var destDirFileNames = getDestDirFileNames(destDirFiles, true);
      var destDirDirectoryNames = getDestDirFileNames(destDirFiles, false);
      for(var i = 0, len = srcFiles.length; i < len; i++){
        var destDir = srcFiles[i]["file"] ? destDirFileNames : destDirDirectoryNames;
        if(destDir.indexOf(srcFiles[i]["name"]) != -1){
            duplicates[d++] = srcFiles[i]["name"];
        }
      }
    }
    createJob(src, dest);
  }

  function createJob(src, dest){
    var job = angular.copy($scope.job);
    job.src = src;
    job.dest = dest;
    var su = _.keys(src.$selected);//[0];
    var du = _.keys(dest.$selected)[0];
    var dest_uris = "";
    var src_uris = "";
    for (var i = 0; i < _.keys(src.$selected).length; i++) {
        if (dest.$selected[du].dir) {
            var n = new URI(su[i]).segment(-1);
            dest_uris += new URI(du).segment(n).toString().trim();
        }
        src_uris += su[i].trim();
        if (i + 1 != _.keys(src.$selected).length) {
            dest_uris += ",";
            src_uris += ",";
        }
    }
    job.dest.uri = dest_uris.replace(/, /g, ",");
    job.src.uri = src_uris.replace(/, /g, ",");
    var modal = null;
    if(duplicates.length > 0) {
      var modal = $modal({
        title: 'Confirm Overwrite',
        contentTemplate: 'transfer-modal.html'
      });
    }
    else {
      var modal = $modal({
        title: 'Transfer',
        contentTemplate: 'transfer-modal.html'
      });
    }

    var strDuplicates = duplicates.length == 0 ? null : duplicates.join(", ");
    modal.$scope.srcUris = src_uris;
    modal.$scope.destUris = dest_uris;
    modal.$scope.duplicates = strDuplicates;
    modal.$scope.job = job;
    modal.$scope.submit = $scope.submit;

    $scope.flag = true;

  }

  var duplicates = [];
  $scope.flag = true;
  $scope.transfer = function (srcName, destName, contents) {
    $scope.flag = false;
    duplicates = [];
    var src = endpoints.get(srcName);
    var dest = endpoints.get(destName);
    if(!$scope.job.options.overwrite) {
      var srcFiles = getSourceFiles(src.$selected);
      getDestDirFiles(dest, srcFiles, src);
    }
    else{
      createJob(src, dest);
    }
  };

  

  $scope.submit = function (job, then) {
    return stork.submit(job).then(
      function (d) {
        if (then)
          then(d);
        $modal({
          title: 'Success!',
          content: 'Job accepted with ID '+d.job_id
        });
        return d;
      }, function (e) {
        if (then)
          then(e);
        $modal({
          title: 'Failed to submit job',
          content: e
        });
        throw e;
      }
    );
  };
  
/* $scope.mk_dir = function (name) {
   //$modalInstance.close(name);
  };
*/


})
