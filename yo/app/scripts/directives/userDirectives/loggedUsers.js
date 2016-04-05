(function() {
    'use strict';

    var app = angular.module('dashboardApp');



    app.directive('loggedUsers', function(){
        return {
            restrict: 'EA',         
            scope: {
                data: '=',                  

            },
            templateUrl: 'views/loggedUsers.html',
            
            controller: "LoggedUsersController as loggedUsersCtrl",
            

            };
    });

    app.controller('LoggedUsersController', function($scope, $uibModal, userService){             
                var vm =this;

                $scope.$watch('data', function(dataObject){
                    if(dataObject){
                        
                        vm.data = dataObject;        
                        
                                           
                    }
                });

                vm.loadUserLocationModal = function(name){          

                      var modalInstance = $uibModal.open({
                        templateUrl :'views/locationModal.html',
                        controller:'LocationModalCtrl',
                        controllerAs:'modalCtrl',
                        size:'lg',
                        resolve :{
                          
                          id : function(){
                            return name;
                          },
                          data: function(){
                            return userService.getUserLocation(name);
                          },
                          entity:function(){
                            return "User";
                          }

                        }

                      });
            }


    });
                    
              


    
})();
