var app = angular.module("app");

app.controller("ConfigController",["$scope", "$http", function($scope, $http){
    $scope.loadProperties = function () {
        $http({method: "GET", url: "/config/" + $scope.currentEnv})
            .then(function(response) {
                updateProperties(response.data)
            })
    }
    function updateProperties(data) {
        for (var index in data) {
            data[index].initValue = data[index].value;
        }
        $scope.parameters = data;
    }

    $scope.store = function(env, name, value) {
        $http({method: "POST", url: "/config/store", data: { env: env, name: name, value: value}})
            .then(function(response) {
                updateProperties(response.data)
            })
    }

    $http({
        method: "GET",
        url: "/config/environments"
    }).then(function(response) {
        $scope.environments = response.data;
        $scope.currentEnv = $scope.environments[0];
        $scope.loadProperties()
    });
}]);