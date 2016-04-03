(function(){
	'use strict';

	/* 
	* A service that provides helper functionality to the dashboards uiGrids.
	*
	*/
	angular.module('dashboardApp').factory('uiGridService', uiGridService);

		    uiGridService.$inject = ['uiGridConstants','$q','$timeout','$filter'];

			function uiGridService (uiGridConstants, $q, $timeout, $filter){				

					

				var services = {
					//Overall service that creates the filterTemplates and all the required lazy loading setup.
					setupGrid : function(gridOptions,  scope, entity, gridDataCall){			        
			          
			            
			            var vm=this;

						//Page for the limit
				        var page = 1;

				        //Amount of results per page;
				        var pageSize = 100;

				        //Used to stop calls when changes in the table occur.
				        var canceller = $q.defer();

				        //Stores sorted columns
				        var sortColumns = [];      

			            gridOptions.pageSize =  100;		            
			            gridOptions.useExternalSorting =  true;
			            gridOptions.useExternalFiltering =  true;
			            gridOptions.enableFiltering = true;
			            gridOptions.enableSelection = false;
			        

			            gridOptions.columnDefs.forEach(function(column){

			            	var field = column.field;
			            	var type = column.type;

			            	if(field ==='query'){

			            		column.cellTemplate = '<div class="ui-grid-cell-contents" uib-tooltip={{row.entity.'+field+'}}>{{row.entity.'+field+'}}</div>';
			            	}

			            	if(type == "bytes"){
			            		column.filterHeaderTemplate ='<div class="ui-grid-filter-container" byte-filter placeholder="To" ng-model="col.filters[0].term"></div>\
			            									 <div class="ui-grid-filter-container" byte-filter placeholder="From" ng-model="col.filters[1].term"></div>';    

			            		column.cellTemplate='<div class="ui-grid-cell-contents">{{row.entity.'+field+'|bytes}}</div>';

			            		column.filters= [
									        
									        { 
									          condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,
									          placeholder: 'From',
									          type:'input'
									        },
									        {
									          condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL,
									          placeholder: 'To',
									          "type": "input"
									        }
									]
							 														 
			            	}
			            	

				            if(!column.filter){
				                if(type == 'number'){

				                	column.filters= [
									        
									        { 
									          condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,
									          placeholder: 'From',
									          type:'input'
									        },
									        {
									          condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL,
									          placeholder: 'To',
									          "type": "input"
									        }
									]

				                }
				            }

				            if(!column.filter){
				                if(type == 'string'){
				                    column.filter = {
				                        "condition": uiGridConstants.filter.CONTAINS,				                        
				                        "type": "input",
				                    }
				                }
				            }
				            if(!column.filters){
				                if(type == 'date'){
				                	column.filterHeaderTemplate ='<div class="ui-grid-filter-container" date-time-picker ng-model="col.filters[0].term" placeholder="To"></div>\
    														 <div class="ui-grid-filter-container" date-time-picker ng-model="col.filters[1].term" placeholder="From" ></div>';
    																		 
				                    column.filters = [
				                        {
									          condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,
									          
								        },
								        {
									          condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL,
									          
								        }
				                    ];

				                    column.cellFilter = "date: 'yyyy-MM-dd HH:mm:ss'"
				                }
				            }



			            });

			            gridOptions.onRegisterApi= function(gridApi){
    		
    						vm.gridApi = gridApi;


				    		vm.gridApi.core.on.sortChanged(scope, vm.sortChanged);
				    		vm.sortChanged(vm.gridApi.grid, [gridOptions.columnDefs[1]]);

				    		vm.gridApi.core.on.filterChanged(scope, function(){
				    			canceller.resolve();
				    			canceller = $q.defer();
				    			
				    			
				    			updateTable().then(function(result){
				    				gridOptions.data = result; 

				    				 
				    				 				

				       			});

				    		});

				    		vm.gridApi.infiniteScroll.on.needLoadMoreData(scope, function() {
				                page++;
				                
				                updateTable().then(function(results){
				                    _.each(results, function(result){ gridOptions.data.push(result); });
				                    
				                    if(results.length == 0) page--;

				                    updateScroll(results.length);
				                });
					        });

					          
					        vm.gridApi.infiniteScroll.on.needLoadMoreDataTop(scope, function() {
					            page--;
					            updateTable().then(function(results){
					                _.each(results, function(result){ gridOptions.data.push(result); });
					                 
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
					    			gridOptions.data = result;
					    		});
					    

					    		
							    
					  		};	

					  		
					  		function updateTable(){

					  			var gridColumns = vm.gridApi.grid.columns;
  			
					  			var queryConstraint = generateQuery(gridColumns, sortColumns, entity);  		

					  			var initialLimit = (page-1)* pageSize;
					  			var maxLimit = pageSize;

					  			return gridDataCall(queryConstraint,initialLimit,maxLimit,canceller)

					  		}


					  		function updateScroll(resultCount){
					            
					            $timeout(function(){
					                var isMore = resultCount == pageSize;
					                if(page == 1) vm.gridApi.infiniteScroll.resetScroll(false, isMore);
					                vm.gridApi.infiniteScroll.dataLoaded(false, isMore);
					            });
					            
					        } 	

	                    


			          return gridOptions;
					
					}	
							
							
				    
				}

			return services;
		}

		function generateQuery(gridColumns, sortColumns, entity){

						var queryConstraint='';

  						var orderBy='';

						gridColumns.forEach(function (columnDef){

							if(!columnDef.field) {return};
	  						


							if(columnDef.colDef.type == 'date' &&  columnDef.filters){
								
								
								var from = columnDef.filters[0].term || '';
								var to = columnDef.filters[1].term || '';
								

														
			                    
			                    if(from != '' || to !=''){
			                        from = completePartialFromDate(from);
                        			to = completePartialToDate(to);
                        			if(queryConstraint !== '') queryConstraint+=' AND ';

                        			queryConstraint+= entity+"."+columnDef.field+" between "+from+" and "+to;                        			
			                        
			                        
			                    }

							}
							else if((columnDef.colDef.type == 'number' || columnDef.colDef.type == 'bytes') && columnDef.filters){
								
								var from = columnDef.filters[0].term || '';
								var to = columnDef.filters[1].term || '';

			                    

			                    if(from != ''|| to != ''){
			                        from = from || '0';
                        			to = to || '1000000000';
			                       	if(queryConstraint !== '') queryConstraint+=' AND ';
                        			queryConstraint+= entity+"."+columnDef.field+" between "+from+" and "+to;
			                        			                        

								}
							}	
							else if(columnDef.colDef.type== 'string' && columnDef.filters[0].term){
								var term = columnDef.filters[0].term;					
								var type = columnDef.field;
								
								if(queryConstraint !== '') queryConstraint+=' AND ';

								//Deal with possible user entity.
								if(type==="fullName"||type==="name") {
									queryConstraint += "user."+type+" like '%"+term+"%'";
								}
								else{
									queryConstraint += entity+"."+type+" like '%"+term+"%'";
								}					
								
							}

					});

		  			
		  			sortColumns.forEach(function(column){
		  				
		  				
		  				if(column.colDef){

		  					if(orderBy!=='') orderBy+=',';
		  					//Deal with the user join.
		  					if(column.colDef.field==="fullName"){
		  						orderBy += 'user.'+column.colDef.field +' '+column.sort.direction;
		  					}
		  					else{  					
		  						orderBy += entity+'.'+column.colDef.field +' '+column.sort.direction;
		  					}
		  					
		  				}
		  			});

		  			if(queryConstraint!=='') queryConstraint= "WHERE "+queryConstraint;

		  			if(orderBy!=='') queryConstraint+=" order by "+orderBy;
		  		
		  			

		  			return queryConstraint;


					}

		function completePartialFromDate(date){
	            var segments = date.split(/[-:\s\/]+/);
	            var year = segments[0];
	            var month = segments[1] || "01";
	            var day = segments[2] || "01";
	            var hours = segments[3] || "00";
	            var minutes = segments[4] || "00";
	            var seconds = segments[5] || "00";

	            year = year + '0000'.slice(year.length, 4);
	            month = month + '00'.slice(month.length, 2);
	            day = day + '00'.slice(day.length, 2);
	            hours = hours + '00'.slice(hours.length, 2);
	            minutes = minutes + '00'.slice(minutes.length, 2);
	            seconds = seconds + '00'.slice(seconds.length, 2);

	            if(parseInt(month) == 0) month = '01';
	            if(parseInt(day) == 0) day = '01';

	            var completeDate= year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;

	            return Date.parse(completeDate);
	    }

        function completePartialToDate(date){
            var segments = date.split(/[-:\s\/]+/);
            var year = segments[0] || "";
            var month = segments[1] || "";
            var day = segments[2] || "";
            var hours = segments[3] || "23";
            var minutes = segments[4] || "59";
            var seconds = segments[5] || "59";
            year = year + '9999'.slice(year.length, 4);
            month = month + '99'.slice(month.length, 2);
            day = day + '99'.slice(day.length, 2);
            hours = hours + '33'.slice(hours.length, 2);
            minutes = minutes + '99'.slice(minutes.length, 2);
            seconds = seconds + '99'.slice(seconds.length, 2);

            if(parseInt(month) > 12) month = '12';
            var daysInMonth = new Date(year, day, 0).getDate();
            if(parseInt(day) > daysInMonth) day = daysInMonth;

            var completeDate= year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;

	        return Date.parse(completeDate);
        }

})();