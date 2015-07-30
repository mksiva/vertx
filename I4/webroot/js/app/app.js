// app.js
var routerApp = angular.module('routerApp', ['ui.router']);

routerApp.config(function ($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/home');

    $stateProvider

            // HOME STATES AND NESTED VIEWS ========================================
            .state('home', {
                url: '/home',
                templateUrl: 'partial-home.html'
            })

            // Payload
            .state('payload', {
                url: '/payload',
                templateUrl: '/views/payloads.html',
                controller: ListCtrl
            })

            // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
            .state('about', {
                // we'll get to this in a bit       
            });
});

routerApp.filter('pagination', function ()
{
    return function (input, start) {
        start = parseInt(start, 10);
        return input.slice(start);
    };
});

function ListCtrl($scope, $http) {
    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;    
    $scope.payloads = []; 
    $http.get('/api/payloads').success(function (data) {     
        $scope.payloads = data; 
        $scope.totalItems = $scope.payloads.length;        
        $scope.range = function () {
        var rangeSize = 10;
        var ps = [];
        var start;
        start = $scope.currentPage;
        if (start > $scope.pageCount() - rangeSize) {
            start = $scope.pageCount() - rangeSize + 1;
        }
        for (var i = start; i < start + rangeSize; i++) {
            ps.push(i);
        }
        return ps;
    };
    
    $scope.firstPage = function () {        
            $scope.currentPage = 0;        
    };
    
    $scope.prevPage = function () {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };
    
     $scope.DisableFirst = function () {
        return $scope.currentPage === 0 ? "disabled" : "";
    };
    
    $scope.DisablePrevPage = function () {
        return $scope.currentPage === 0 ? "disabled" : "";
    };
    
    $scope.pageCount = function () {        
        return Math.ceil($scope.payloads.length / $scope.itemsPerPage) - 1;
    };

    $scope.lastPage = function () {        
            $scope.currentPage = $scope.pageCount();        
    };
    
    $scope.nextPage = function () {
        if ($scope.currentPage < $scope.pageCount()) {
            $scope.currentPage++;
        }
    };

    $scope.DisableLast = function () {
        return $scope.currentPage === $scope.pageCount() ? "disabled" : "";
    };
    
    $scope.DisableNextPage = function () {
        return $scope.currentPage === $scope.pageCount() ? "disabled" : "";
    };

    $scope.setPage = function (n) {
        $scope.currentPage = n;
    };
        
    });

    
}
