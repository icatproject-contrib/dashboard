(function(){
	'use strict';

	/* 
	* A service that provides helper functionality to the dashboards uiGrids.
	*
	*/
	angular.module('dashboardApp').factory('uiGridService', uiGridService);

		    uiGridService.$inject = ['uiGridConstants'];

			function uiGridService (uiGridConstants){				

					

				var services = {

					setupGrid : function(gridOptions){			        
			          
			            
			            
			            gridOptions.pageSize =  100;		            
			            gridOptions.useExternalSorting =  true;
			            gridOptions.useExternalFiltering =  true;
			            gridOptions.enableFiltering = true;
			            gridOptions.enableSelection = false;
			        

			            gridOptions.columnDefs.forEach(function(column){

			            	var field = column.field;
			            	var type = column.type;

			            	column.cellTemplate = '<div class="ui-grid-cell-contents" uib-tooltip={{row.entity.'+field+'}}>{{row.entity.'+field+'}}</div>';

			            	if(type== 'date'){
			            		column.filterHeaderTemplate ='<div class="ui-grid-filter-container" date-time-picker ng-model="col.filters[0].term"></div>\
    														 <div class="ui-grid-filter-container" date-time-picker ng-model="col.filters[1].term"></div>';
			            	}

			            	if(column.filter && typeof column.filter.condition == 'string'){
				            	column.filter.condition = uiGridConstants.filter[column.filter.condition.toUpperCase()];
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
				                    column.filters = [
				                        {
				                        	"condition": 'GREATER_THAN_OR_EQUAL',
				                            "placeholder": "Start",
				                            "type": "input"
				                        },
				                        {
				                        	"condition": 'LESS_THAN_OR_EQUAL',
				                            "placeholder": "End",
				                            "type": "input"
				                        }
				                    ];
				                }
				            }



			            });

                    


			          return gridOptions;
					
					}
					
					
				    
				}

			return services;
		}

})();