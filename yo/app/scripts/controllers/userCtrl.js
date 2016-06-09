(function (){
	  'use strict';
angular.module('dashboardApp').controller('UserCtrl', UserCtrl);

UserCtrl.$inject= ['$scope','googleChartApiPromise', 'userService','uiGridService','$uibModal',"$q","$rootScope"];	


function UserCtrl($scope,googleChartApiPromise, userService, uiGridService,$uibModal,$q,$rootScope){		
		
    		var vm=this;		
    	 
        vm.userOption = true;



       //Setup the ui-grid for log tables.

        vm.gridOptions = {}
        
        vm.gridOptions.columnDefs = [
        	{field: 'id',  displayName: 'Location', width:80, type:'button', cellTemplate:'<div class="button-holder" ng-if="row.entity.ipAddress" align=center><button class="btn btn-default btn-large text-center" ng-click="grid.appScope.loadGridLocationModal(row.entity.id)"><span class="glyphicon glyphicon-globe" aria-hidden="true"></span></button></div>' },
        	{field: 'entityId', type:'number', displayName: 'Entity ID', width:80},
        	{field: 'entityType',  type:"dropdown", selectOptions:$rootScope.entityOptions, displayName: 'Entity Type', width:140},        	
        	{field: 'ipAddress', type:"string", displayName: 'ipAddress', width:120 },        	
        	{field: 'duration', type:'number', displayName:'Duration', width:110 },
        	{field: 'operation',  type:"dropdown", selectOptions:$rootScope.logOperations, displayName:'Operation', width:100 },
    		  {field: 'query', type:"string", displayName:'Query' },
    		  {field: 'fullName',  type:"join", displayName:'User', width:110},
    		  {field: 'logTime',  type:"date",  displayName: 'Log Time',width:160},


    		
    	];


      function gridDataCall(queryConstraint,initialLimit,maxLimit,canceller){
        
        return userService.getIcatLogs(queryConstraint,initialLimit,maxLimit,canceller);

      }

      vm.gridOptions = uiGridService.setupGrid(vm.gridOptions,$scope,"log", gridDataCall);  	


    //Initialise the page with the values it requires for the menus
      vm.initPage = function(){
        //Set default dates
        vm.endDate = new Date();

        vm.startDate = new Date(new Date().setDate(new Date().getDate()-10)); 

        vm.updatePage();
      }

       //Updates the date and the page
        vm.updateOptions = function(startDate,endDate,userName){
          vm.startDate = startDate;
          vm.endDate = endDate;
          vm.userName = userName;          
      
          vm.updatePage();

        }  

        vm.updatePage = function(){ 

           var user = vm.userName;

           var loggedFrequencyPromise = vm.updateLoggedFrequency(user);

           var groupPromise = $q.all([loggedFrequencyPromise]);

           groupPromise.then(function(responseData){
              

              vm.dataCsv = [
                  {
                    type:"userLoginCount",
                    title:"User login frequency. User "+user,
                    data:responseData[0],
                  }                  
              ]
           });

        }


        vm.updateLoggedFrequency = function(userName){

          return userService.getLoggedFrequency(getStartDate(),getEndDate(),userName).then(function(responseData){

            var formattedData = formatData(responseData); 


            vm.loginFrequency = {
              data:{
                x:"x",           
                      columns : formattedData,
                    types:{
                      Number:'line',
                    }
                  },
              description : "This line graph shows the frequency of user logins per day.",   
              title: "Number of user logins per day.",           
              zoom:true,
              xLabel:"Login Date",
              yLabel:"Number of Logins",
            
              
              } 


            return responseData;

          });
        }

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


      function getStartDate(){

        var startDate = moment(vm.startDate).subtract(1,'seconds');         

        startDate.set('hour','00');
        startDate.set('minute','00');
        startDate.set('second','00');

      return startDate;

    }   

    //Gets the end date with its formatted values
    function getEndDate(){
             
        return Date.parse(vm.endDate);
      
      } 

      //Formats data where the values are date and number
        function formatData(data){


          var dates  = _.map(data, function(data){
          return data.date;
        });

        var numbers = _.map(data, function(data){
          return data.number;
        });

        dates.unshift("x");   
            
        numbers.unshift('Number');   


        return [dates,numbers];

        }  


		
	}

	

})();