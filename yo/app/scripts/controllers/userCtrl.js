(function (){
	  'use strict';
angular.module('dashboardApp').controller('UsersCtrl', UsersCtrl);

UsersCtrl.$inject= ['$scope','googleChartApiPromise','$q', 'uiGridConstants','userService','$timeout','uiGridService'];	


function UsersCtrl($scope,googleChartApiPromise,$q, uiGridConstants, userService, $timeout,uiGridService){		
		
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

        

        //Page for the limit
        var page = 1;

        //Amount of results per page;
        var pageSize = 100;

        //Used to stop calls when changes in the table occur.
        var canceller = $q.defer();

        //Stores sorted columns
        var sortColumns = [];

        var filters = "";

        vm.gridOptions = {}
        
        vm.gridOptions.columnDefs = [
        	{field: 'id', displayName: 'ID', type:'string', width:80 },
        	{field: 'entityId', type:'string', displayName: 'Entity ID', width:80},
        	{field: 'entityType',  type:"string", displayName: 'Entity Type', width:140},        	
        	{field: 'ipAddress', type:"string", displayName: 'ipAddress', width:120 },        	
        	{field: 'duration', type:'string', displayName:'Duration', width:110 },
        	{field: 'op',  type:"string", displayName:'Operation', width:100 },
    		  {field: 'query', type:"string", displayName:'Query' },
    		  {field: 'fullName',  type:"string", displayName:'User', width:110},
    		  {field: 'logTime',  type:"date", displayName: 'Log Time',width:160}	
    		
    	];

      vm.gridOptions = uiGridService.setupGrid(vm.gridOptions);   	



    	vm.gridOptions.onRegisterApi= function(gridApi){
    		
    		vm.gridApi = gridApi;

    		vm.gridApi.core.on.sortChanged($scope, vm.sortChanged);
    		vm.sortChanged(vm.gridApi.grid, [vm.gridOptions.columnDefs[1]]);

    		vm.gridApi.core.on.filterChanged($scope, function(){
    			canceller.resolve();
    			canceller = $q.defer();
    			console.log("change")

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
    		sortColumns = passedSortColumns;

    		updateTable().then(function(result){
    			vm.gridOptions.data = result;
    		});
    

    		
		    
  		};	

  		function updateTable(){
  			var gridColumns = vm.gridApi.grid.columns;
  			
  			var queryConstraint='';

  			var orderBy='';

  			gridColumns.forEach(function (columnDef){  			

  				if(!columnDef.field) {return};
  				

				if(columnDef.colDef.type == 'date' &&  columnDef.filters){
					console.log(columnDef.filters)
					
					var from = columnDef.filters[0].term || '';
				
                    
                    if(from != ''){
                        
                       
                        console.log(from)
                        
                        
                    }

				}
				else if(columnDef.colDef.type == 'number' && columnDef.filters){
					console.log(columnDef)
					var from = columnDef.filters[0].term || '';
                    

                    if(from != ''){
                        from = parseInt(from || '0');
                       

                        console.log(from)
                        

					}
				}	
				else if(columnDef.colDef.type== 'string' && columnDef.filters[0].term){
					var term = columnDef.filters[0].term;					
					var type = columnDef.field;
					
					if(queryConstraint !== '') queryConstraint+=' AND ';

					//Deal with possible user entity.
					if(type==="fullName") {
						queryConstraint += "user."+type+" like '%"+term+"%'";
					}
					else{
						queryConstraint += "log."+type+" like '%"+term+"%'";
					}					
					
				}
				  
			});

  			
  			sortColumns.forEach(function(column){
  				
  				
  				if(column.colDef){

  					if(orderBy!=='') orderBy+=',';
  					//Deal with the user join.
  					if(column.colDef.field==="fullName"){
  						orderBy += ' user.'+column.colDef.field +' '+column.sort.direction;
  					}
  					else{  					
  						orderBy += ' log.'+column.colDef.field +' '+column.sort.direction;
  					}
  					
  				}
  			});

  			if(queryConstraint!=='') queryConstraint= "WHERE "+queryConstraint;

  			if(orderBy!=='') queryConstraint+=" order by"+orderBy;
  		

  			var initialLimit = (page-1)* pageSize;
  			var maxLimit = pageSize;

  			return userService.getIcatLogs(queryConstraint,initialLimit,maxLimit,canceller);

  		}

  		function updateScroll(resultCount){
            
            $timeout(function(){
                var isMore = resultCount == pageSize;
                if(page == 1) vm.gridApi.infiniteScroll.resetScroll(false, isMore);
                vm.gridApi.infiniteScroll.dataLoaded(false, isMore);
            });
            
        }



        vm.updatePage = function(){	



        }


		
	}

	

})();
