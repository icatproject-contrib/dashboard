(function (){
	  'use strict';
angular.module('dashboardApp').controller('UserCtrl', UserCtrl);

UserCtrl.$inject= ['$scope','googleChartApiPromise', 'userService','uiGridService','$uibModal'];	


function UserCtrl($scope,googleChartApiPromise, userService, uiGridService,$uibModal){		
		
    		var vm=this;		
    		
    		vm.format =  'yyyy-MM-dd';

    		vm.endDate = new Date();
    	
    		vm.startDate = new Date(new Date().setDate(new Date().getDate()-10));		

		    //Date selection for the overall page
		    vm.isStartDateOpen = false;
        vm.isEndDateOpen = false;       

		    vm.openStartDate = function(){
            vm.isStartDateOpen = true;
            vm.isEndDateOpen = false;
        };

        vm.openEndDate = function(){
        	
            vm.isStartDateOpen = false;
            vm.isEndDateOpen = true;
        };

       //Setup the ui-grid for log tables.

        vm.gridOptions = {}
        
        vm.gridOptions.columnDefs = [
        	{field: 'id',  displayName: 'Location', width:80, type:'button', cellTemplate:'<div class="button-holder" ng-if="row.entity.ipAddress" align=center><button class="btn btn-default btn-large text-center" ng-click="grid.appScope.loadGridLocationModal(row.entity.id)"><span class="glyphicon glyphicon-globe" aria-hidden="true"></span></button></div>' },
        	{field: 'entityId', type:'number', displayName: 'Entity ID', width:80},
        	{field: 'entityType',  type:"string", displayName: 'Entity Type', width:140},        	
        	{field: 'ipAddress', type:"string", displayName: 'ipAddress', width:120 },        	
        	{field: 'duration', type:'number', displayName:'Duration', width:110 },
        	{field: 'operation',  type:"string", displayName:'Operation', width:100 },
    		  {field: 'query', type:"string", displayName:'Query' },
    		  {field: 'fullName',  type:"string", displayName:'User', width:110},
    		  {field: 'logTime',  type:"date",  displayName: 'Log Time',width:160},


    		
    	];


      function gridDataCall(queryConstraint,initialLimit,maxLimit,canceller){
        
        return userService.getIcatLogs(queryConstraint,initialLimit,maxLimit,canceller);

      }

      vm.gridOptions = uiGridService.setupGrid(vm.gridOptions,$scope,"log", gridDataCall);  	


  		//Use of scope is to allow the ui-grid to access the call.
      $scope.loadGridLocationModal = function(logId,entity){
          

          var modalInstance = $uibModal.open({
            templateUrl :'views/locationModal.html',
            controller:'LocationModalCtrl',
            controllerAs:'modalCtrl',
            size:'lg',
            resolve :{
              
              data :function(){
                 return userService.getIcatLogLocation(logId);                   
                
              },
              id:function(){
                return logId;
              },
              entity:function(){
                return "log";
              }

            }

          });
        }


  	   //Gather logged in users

       userService.getLoggedUsers().then(function(responseData){ 
          vm.loggedUsers = responseData;

       });



		
	}

	

})();