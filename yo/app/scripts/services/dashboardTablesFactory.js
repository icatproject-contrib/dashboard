
(function() {
    'use strict';

    var app = angular.module('dashboardApp');

    app.factory('dashboardTables', function(){
    	return {
			'tables': {
				'log':{
					'columns': {
						'id': 'string',
						'entityId': 'string',
						'entityType': 'string',
						'ipAddress': 'string',
						'duration':'number',
						'op':'string',
						'query':'string',
						'fullName':'string',
						'logTime':'date'
					}
				},				
				
				'download': {
					'columns': {			
						
						'fullName': 'string',
						'id': 'string',
						'name': 'string',
						'bandiwdth': 'number',
						'size': 'number',
						'method': 'string',
						'start': 'date',
						'end': 'date',
						'status': 'string'
						
					}
				},
				'downloadItem': {
					'columns': {
						'icatId': 'number',
						'name': 'string',
						'size': 'number',
						'type': 'string',
						'creationTime': 'date'
					}
				},
			}
		};
    });
    	

})();
