angular.module('dashboardApp').controller('DownloadModalCtrl', function($scope,$uibModalInstance , downloadData, downloadId){

		var vm = this;

		vm.downloadId = downloadId;	

		
		vm.gridOptions = {};
		vm.gridOptions.data = downloadData;
		vm.gridOptions.enableFiltering = true; 
        vm.gridOptions.columnDefs = [
       		{field: 'icatId', displayName: 'ID in ICAT',width:100},
        	{field: 'name', displayName: 'Entity Name', width:200 },        	
        	{field: 'size', width:100,displayName: 'Size', cellTemplate:'<div class="ui-grid-cell-contents">{{row.entity.size|bytes}}</div>'},
        	{field: 'type',  displayName: 'Entity Type',width:100},
        	{field: 'creationTime',  displayName: 'Creation Time',cellFilter:'date:"medium"'},
        	
    		
    	];
		

		vm.close = function (){
			 $uibModalInstance.dismiss('close');
		}
		

		
	});

