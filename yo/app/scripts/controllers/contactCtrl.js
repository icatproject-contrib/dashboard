(function() {
	'use strict';
	angular.module('dashboardApp').controller('ContactCtrl', ContactCtrl);

	ContactCtrl.$inject= ['contactService', '$q'];	

	function ContactCtrl(contactService, $q){	
            var vm=this;
            vm.ready = false;
            
            vm.getContactMessage = function() {
                return contactService.getContactMessage().then(function(responseData) {                    
                    var contactMessage = _.map(responseData, function(data) {
                        return data.message;
                    });
            
                    vm.contactMessage = {
                        message:contactMessage.toString()
                    };

                });	
            }
            
            var getContactMessagePromise = vm.getContactMessage();
            var groupPromise = $q.all([getContactMessagePromise])
            
            groupPromise.then(function(){
                vm.ready = true;
            });          
        }
        
        angular.module('dashboardApp').filter('jsonFilter', function () {
            return function(input) {
                if(!input) return "";
                var length = input.toString().length;
                return input.toString().substring(2, (length - 2));
            };
        });
        
})();