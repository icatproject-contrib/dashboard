angular.module('dashboardApp').controller('DownloadModalCtrl', function($scope,$uibModalInstance , downloadId, uiGridService, downloadService,$q,$timeout){

  		var vm = this;

  		vm.downloadId = downloadId;	

   		
      vm.gridOptions = {}

      vm.gridOptions.columnDefs = [
     		{field: 'icatId', displayName: 'ID in ICAT',width:100, type:'number'},
      	{field: 'entityName', displayName: 'Entity Name', width:200,  type:'string' },        	
      	{field: 'entitySize', width:130,displayName: 'Size',  type:"bytes"},
      	{field: 'type',  displayName: 'Entity Type',width:100, type:'string'},
      	{field: 'creationTime',  displayName: 'Creation Time',type:'date'},
      	
  		
  	];

     function gridDataCall(queryConstraint,initialLimit,maxLimit,canceller){

      

      if(queryConstraint==''){
        queryConstraint = " WHERE download.id="+downloadId; 
      }
      else if(queryConstraint.indexOf("order") > -1){
         queryConstraint = " WHERE download.id="+downloadId +" "+queryConstraint;
      }
      else{
        queryConstraint = [queryConstraint.slice(0,5),"  download.id="+downloadId+" AND", queryConstraint.slice(5)].join(''); 
      }
      
      return downloadService.getDownloadEntities(queryConstraint,initialLimit,maxLimit,canceller);

    }   

  	 vm.gridOptions = uiGridService.setupGrid(vm.gridOptions, $scope,"entity", gridDataCall); 


   



  vm.close = function (){
  	 $uibModalInstance.dismiss('close');
  }



  });

