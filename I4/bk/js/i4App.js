/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var i4App = angular.module('i4App', ['ui.router']).
        config('$stateProvider','$urlRouterProvider',function($stateProvider,$urlRouterProvider){
                $urlRouterProvider.otherwise("/");
                $stateProvider                        
                .state('home', {
                    url: "/home", 
                    templateUrl: "/tpl/payloads.html", 
                    controller: ListCtrl
                })
            }
        );

function ListCtrl($scope, $http) {
    $http.get('/api/payloads').success(function (data) {
        $scope.payloads = data;
    });
}




