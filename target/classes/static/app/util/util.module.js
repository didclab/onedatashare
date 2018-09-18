'use strict';

var forceAsyncEvents = {
  'blur': true,
  'focus': true
};

/** Various utilities used throughout the application. */
angular.module('stork.util', [
  'mgcrea.ngStrap.tooltip'
])

/** Filter to make byte counts human-readable. */
.filter('size', function () {
  return function (bytes, precision) {
    var b = bytes     || 0
    var p = precision || 0
    var s = 'kMGTPEZY'
    for (var i = 0; i < s.length && b >= 1000; i++) {
      b /= 1000
      var c = s.charAt(i)
    }
    return c ? b.toFixed(p)+c : b.toFixed(0)
  };
})

/** Filter to convert a float into a percentage. */
.filter('percent', function () {
  return function (p, precision, symbol) {
    if (!p || p.done < 0 || p.total <= 0) return
    var d = p.done
    var t = p.total
    var p = precision || 0
    var n = 100*d/t
    n = (n < 0) ? 0 : (n > 100) ? 100 : n
    return n.toFixed(p)+(symbol||'%')
  };
})

/** Filter to convert {done:..., total: ...} -> 'done/total'. */
.filter('progress', function ($filter) {
  return function (p, precision, symbol) {
    var size = $filter('size');
    if (!p) return;
    var t = size(p.total);
    var d = size(p.done);
    return d+'/'+t;
  };
})
/** Filter to get the values of an object. */
.filter('values', function () {
  return _.values;
})

/** Filter to get the keys of an object. */
.filter('keys', function () {
  return _.keys;
})

/** Filter to get a list of key:value pairs in an object. */
.filter('pairs', function () {
  return _.pairs;
})

/** Copy the passed object. */
.filter('copy', function () {
  return angular.copy;
})

/** Filter to make a time human-readable. */
.filter('moment', function () {
  return function (input, format) {
    return moment(input, format).fromNow();
  };
})

/** Filter to normalize a URI. */
.filter('URI', function () {
  return function (input) {
    return new URI(input);
  };
})

/** Filter to paginate? Where is this used? */
.filter('paginate', function () {
  return function (input, page, per) {
    page = (!page || page < 1) ? 1  : page;
    per  = (!per  || per  < 1) ? 10 : per;
    return input.slice(per*(page-1), per*page);
  };
})

.filter('yorn', function () {
  return function (input) {
    return input ? '\u2713' : '\u2718';
  };
})

.filter('lidisplay', function() {
  return function (input) {
    return input == 0 ? 'N/A' : input;
  };
})

/** Directive to add an active class if the route is selected. */
.directive('bsRoute', function ($location) {
  return {
    link: function (scope, elm, attrs) {
      var check = function () {
        if ($location.path() == attrs.bsRoute)
          elm.addClass('active');
        else
          elm.removeClass('active');
      };
      scope.$on('$routeChangeSuccess',
        function (event, current, previous) {
          check();
        }
      );
    }
  };
})

/** Directive that focuses something when parsed. */
.directive('focusMe', function ($timeout) {    
  return {    
    link: function (scope, element, attrs, model) {                
      $timeout(function () {
        element[0].focus();
      }, 20);
    }
  };
})

/** Automatically make anything with a title use tooltips. */
.directive('title', function ($tooltip, $interpolate) {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      var title = $interpolate(element.attr('title'))(scope);
      var tip = $tooltip(element, {title: title});

      element.on('mouseover', function () {
        var title = element.attr('title');
        element.removeAttr('title');
        element.attr('data-title', title);
      });

      element.on('mouseleave', function () {
        var title = element.attr('data-title');
        element.removeAttr('data-title');
        element.attr('title', title);
      });
    }
  };
})

/** MDN Web API Interface */
/** inherite parent scope here */
.directive('draggable', function () {
  return {   
    /** link to add event listener */
    link: function (scope, element, attrs) {
      element[0].addEventListener("dragstart",scope.storkDragStart,false);
      element[0].addEventListener("dragend",scope.storkDragEnd,false);
      element[0].addEventListener("dragenter", scope.storkDragEnter, false);
      element[0].addEventListener("dragleave",scope.storkDragLeave, false);
    }
  };
})

.directive('droppable', function() {
  return {
    restrict: 'A',
    link: function(scope, element, attrs) {
      element[0].addEventListener("drop",scope.storkDrop,false);
      element[0].addEventListener("dragover", scope.storkDragOver,false);
    }    
  };
})

.directive('liDrop',  function ($parse, $rootScope) {
  return {
     link: function(scope, element, attrs) {
          var fn =  $parse (attrs.liDrop);
          element.on('drop', function(event) {
              var callback = function() {
                fn(scope, {$event:event});
              };
                scope.$apply(callback);
          });
      }
  }
});

/*!important (for exercise - how to create a directive that can handle event, can bind event handler - here is a function - for events - here is "mousedown", and have $event as an argument)**/
/*.directive('liM',  function ($parse, $rootScope) {
  return {
     link: function(scope, element, attrs) {
          var fn =  $parse (attrs.liM);
          element.on('mousedown', function(event) {
              var callback = function() {
                fn(scope, {$event:event});
              };
                scope.$apply(callback);
          });
      }
  }
});*/

