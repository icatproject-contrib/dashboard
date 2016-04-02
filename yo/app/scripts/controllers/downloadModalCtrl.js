angular.module('dashboardApp').controller('DownloadModalCtrl', function($scope,$uibModalInstance , downloadId, uiGridService, downloadService,$q,$timeout){

		var vm = this;

		vm.downloadId = downloadId;	

 		//Page for the limit
        var page = 1;

        //Amount of results per page;
        var pageSize = 100;

        //Used to stop calls when changes in the table occur.
        var canceller = $q.defer();

        //Stores sorted columns
        var sortColumns = [];      

        vm.gridOptions = {}
	
        vm.gridOptions.columnDefs = [
       		{field: 'icatId', displayName: 'ID in ICAT',width:100, type:'number'},
        	{field: 'entityName', displayName: 'Entity Name', width:200,  type:'string' },        	
        	{field: 'size', width:130,displayName: 'Size',  type:'bytes', cellTemplate:'<div class="ui-grid-cell-contents">{{row.entity.size|bytes}}</div>'},
        	{field: 'type',  displayName: 'Entity Type',width:100, type:'string'},
        	{field: 'creationTime',  displayName: 'Creation Time',type:'date'},
        	
    		
    	];
		
    	 vm.gridOptions = uiGridService.setupGrid(vm.gridOptions);   	


      
    	vm.gridOptions.onRegisterApi= function(gridApi){
    		
    		vm.gridApi = gridApi;

    		vm.gridApi.core.on.sortChanged($scope, vm.sortChanged);
    		vm.sortChanged(vm.gridApi.grid, [vm.gridOptions.columnDefs[1]]);

    		vm.gridApi.core.on.filterChanged($scope, function(){
    			canceller.resolve();
    			canceller = $q.defer();
    			

    			updateTable().then(function(result){
    				vm.gridOptions.data = result;    				

       			});

    		});

    		vm.gridApi.infiniteScroll.on.needLoadMoreData($scope, function() {
                page++;
                
                updateTable().then(function(results){
                    _.each(results, function(result){ vm.gridOptions.data.push(result); });
                    
                    if(results.length == 0) page--;

                    updateScroll(results.length);
                });
        });

          
        vm.gridApi.infiniteScroll.on.needLoadMoreDataTop($scope, function() {
            page--;
            updateTable().then(function(results){
                _.each(results, function(result){ vm.gridOptions.data.push(result); });
                 
                if(results.length == 0) page++;

                updateScroll(results.length);
            });
        });

    		
    	}


    	vm.sortChanged = function(grid, passedSortColumns){
        	canceller.resolve();
       		canceller = $q.defer();

    		sortColumns = passedSortColumns;

    		updateTable().then(function(result){
    			vm.gridOptions.data = result;
    		});
    

    		
		    
  		};	

  		function updateTable(){
  			var gridColumns = vm.gridApi.grid.columns;
  			
  			var queryConstraint = uiGridService.generateQuery(gridColumns, sortColumns, "entity"); 


  			if(queryConstraint==''){
  				queryConstraint = " WHERE download.id="+downloadId; 
  			}
  			else{
  				queryConstraint = [queryConstraint.slice(0,5),"  download.id="+downloadId+" AND", queryConstraint.slice(5)].join(''); 
  			}
  					

  			var initialLimit = (page-1)* pageSize;
  			var maxLimit = pageSize;


  			return downloadService.getDownloadEntities(queryConstraint,initialLimit,maxLimit,canceller);

  		}

  		function updateScroll(resultCount){
            
            $timeout(function(){
                var isMore = resultCount == pageSize;
                if(page == 1) vm.gridApi.infiniteScroll.resetScroll(false, isMore);
                vm.gridApi.infiniteScroll.dataLoaded(false, isMore);
            });
            
        }

		vm.close = function (){
			 $uibModalInstance.dismiss('close');
		}
		

		
	});

