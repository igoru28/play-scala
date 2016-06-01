angular.module("app")
    .controller("DeployController", ['$scope', '$http', function ($scope, $http) {
        $scope.applications = []
        $http({method: "GET", url: "/apps"})
            .then(function(response) {
                $scope.applications = response.data
            })
    }
])