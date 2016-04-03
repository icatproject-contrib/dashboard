(function (){
	  'use strict';
angular.module('dashboardApp').controller('UsersCtrl', UsersCtrl);

UsersCtrl.$inject= ['$scope','googleChartApiPromise', 'userService','uiGridService'];	


function UsersCtrl($scope,googleChartApiPromise, userService, uiGridService){		
		
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

      

        vm.gridOptions = {}
        
        vm.gridOptions.columnDefs = [
        	{field: 'id', displayName: 'ID', type:'number', width:80 },
        	{field: 'entityId', type:'number', displayName: 'Entity ID', width:80},
        	{field: 'entityType',  type:"string", displayName: 'Entity Type', width:140},        	
        	{field: 'ipAddress', type:"string", displayName: 'ipAddress', width:120 },        	
        	{field: 'duration', type:'number', displayName:'Duration', width:110 },
        	{field: 'op',  type:"string", displayName:'Operation', width:100 },
    		  {field: 'query', type:"string", displayName:'Query' },
    		  {field: 'fullName',  type:"string", displayName:'User', width:110},
    		  {field: 'logTime',  type:"date",  displayName: 'Log Time',width:160}	
    		
    	];


      function gridDataCall(queryConstraint,initialLimit,maxLimit,canceller){
        
        return userService.getIcatLogs(queryConstraint,initialLimit,maxLimit,canceller);

      }

      vm.gridOptions = uiGridService.setupGrid(vm.gridOptions,$scope,"log", gridDataCall);  	

  		

  	

        vm.updatePage = function(){	



        }


		
	}

	

})();
