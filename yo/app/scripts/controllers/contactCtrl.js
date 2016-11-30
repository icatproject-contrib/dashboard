(function() {
	'use strict';
	angular.module('dashboardApp').controller('ContactCtrl', ContactCtrl);

	ContactCtrl.$inject= ['contactService', '$q'];	

	function ContactCtrl(contactService, $q){	
            var vm=this;
            vm.ready = false;
            
            vm.getContactMessage = function() {
                return contactService.getContactMessage().then(function(responseData) {                    
                    var contactMessage = _.map(responseData, function(data){
                        return data.message;
                    });
                    
                    vm.contactMessage = {
                        message:contactMessage
                    }

                });	
            }
            
            var getContactMessagePromise = vm.getContactMessage();
            var groupPromise = $q.all([getContactMessagePromise])
            
            groupPromise.then(function(){
                vm.ready = true;
            });

                      
        }
})();