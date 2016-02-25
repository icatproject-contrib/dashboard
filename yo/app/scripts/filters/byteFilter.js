(function() {
    'use strict';

    /**
     * This is a wrapper filter for pretty bytes. The pretty bytes does not take a string
     * so we have to do a parseInt
     */
    angular.module('bytes', []). filter('bytes', ['$filter', function( $filter) {


            return function(value) {
            	
                var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
		   		if (value == 0) return '0 Byte';

		   		var i = parseInt(Math.floor(Math.log(value) / Math.log(1024)));

		   		return Math.round(value / Math.pow(1024, i), 2) + ' ' + sizes[i];

                
            };
        }]);
})();