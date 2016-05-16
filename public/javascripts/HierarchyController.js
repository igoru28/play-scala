var app = angular.module("app", []);

app.factory('data', ['$http', function ($http) {
    var ret = {
        responses: {},
        refresh: function () {
            $http({method: 'GET', url: '/actors'})
                .then(function (response) {
                    ret.rootNode = response.data;
                    $http({method: 'GET', url: '/messages'})
                        .then(function(response) {
                            ret.commonResponses = []
                            for (var i in response.data) {
                                var path = response.data[i].path;
                                var msg = response.data[i].message;
                                if (msg && msg.length > 0) {
                                    ret.responses[path] = msg
                                    ret.commonResponses.push({path: path, message: msg})
                                }
                            }
                            //angular.forEach(response.data, function(path, message) {
                            //    if (message && message.length > 0) {
                            //        ret.messages[path] = message
                            //    }
                            //});
                        })
                })
        },
        add: function (path) {
            $http({method: 'POST', url: '/addChild', data: {path: path}})
                .finally(ret.refresh)
        },
        quit: function (path) {
            $http({method: 'POST', url: '/quit', data: {path: path}})
                .finally(ret.refresh)
        },
        send: function(path) {
            $http({method: 'POST', url: '/ping', data: {path: path, message: ret.messages[path]}})
                .finally(ret.refresh)
        },
        sendTo: function(path, message) {
            $http({method: 'POST', url: '/ping', data: {path: path, message: message}})
                .finally(ret.refresh)
        },
        throwException: function(path) {
            $http({method: 'POST', url: '/throwException', data: {path: path, message: ret.messages[path]}})
                .finally(ret.refresh)
        },
        throwExceptionTo: function(path, message) {
            $http({method: 'POST', url: '/throwException', data: {path: path, message: message}})
                .finally(ret.refresh)
        },
        messages: {}
    };
    ret.refresh();
    return ret;
}]).controller("HierarchyController", ['$scope', '$http', "$interval", 'data',
    function ($scope, $http, $interval, data) {
        $interval(data.refresh, 10000)

        $scope.data = data
    }
]).directive("actorNode", ['$http', '$timeout', '$interval', 'data', function ($http, $timeout, $interval, data) {
    var ret = {
        restrict: 'EA',
        templateUrl: "assets/actor-node.html",
        scope: {
            node: "=node",
            root: "=root"
        },
        blink: function(scope) {
            ret.show(scope);
            $timeout(function() { ret.hide(scope)}, 10000);
        },
        show: function(scope) {
            scope.showAlert = true;
        },
        hide: function(scope) {
            scope.showAlert = false;
        },
        link: function (scope) {
            scope.data = data
            scope.showAlert = false;
            scope.$watch('data.responses[node.path]', function(newValue) {
                if (newValue && newValue.length > 0) {
                    scope.responseMsg = newValue;
                    $interval(function() {ret.blink(scope)}, 800, 5);
                    delete data.responses[scope.node.path]
                }
            })
        }
    };
    return ret;
}]);