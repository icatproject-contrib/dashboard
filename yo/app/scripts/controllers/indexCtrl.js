(function (){

	angular.module('dashboardApp')
		   .controller('IndexController', IndexController);

	IndexController.$inject = ['$sessionStorage','$rootScope'];

	function IndexController($sessionStorage,$rootScope){

		
		
		var ic = this;

		ic.isLoggedIn = function(){
			return !(_.isEmpty($sessionStorage.sessionData));
		}

		$rootScope.baseURL = '/dashboard/';

		$rootScope.graphColours = ["#2980B9","#4B77BE","#3498DB"];

		$rootScope.logOperations = [ 
		{ value: 'search', label: 'Search' }, 
		{ value: 'login', label: 'Login' }, 
		{ value: 'logout', label: 'Logout'}, 
		{ value: 'get', label: 'Get' }, 
		{ value: 'refresh', label: 'Refresh' },
		{ value: 'createMany', label: 'Create Many' },
		{ value: 'deleteMany', label: 'Delete Many' },
		{ value: 'update', label: 'Update' },
		{ value: 'delete', label: 'Refresh' },
		{ value: 'searchText', label: 'Search Text' },
		{ value: 'create', label: 'Create' } ]


		$rootScope.entityOptions = [
			{value:"APPLICATION" ,label:"Application"}, 
			{value: "DATACOLLECTION" ,label:"DataCollection"}, 
			{value:"DATACOLLECTIONDATAFILE",label:"DataCollectionDatafile"}, 
			{value:"DATACOLLECTIONDATASET",label:"DataCollectionDataset"}, 
			{value:"DATACOLLECTIONPARAMETER", label:"DataCollectionParameter"}, 
			{value: "DATAFILE", label:"Datafile"},
			{value:"DATAFILEFORMAT", label:"DatafileFormat"},
			{value:"DATAFILEPARAMETER",label: "DatafileParameter"}, 
			{value:"DATASET",label:"Dataset"}, 
			{value:"DATASETPARAMETER", label:"DatasetParameter"}, 
			{value:"DATASETTYPE",label:"DatasetType"}, 
			{value:"FACILITY",label:"Facility"}, 
			{value:"FACILITYCYCLE",label:"FacilityCycle"}, 
			{value:"GROUPING", label:"Grouping"}, 
			{value:"INSTRUMENT", label:"Instrument"},
			{value:"INSTRUMENTSCIENTISTS", label:"InstrumentScientist"}, 
			{value:"INVESTIGATION", label:"Investigation"}, 
			{value:"INVESTIGATIONGROUP",label:"InvestigationGroup"}, 
			{value:"INVESTIGATIONINSTRUMENT", label:"InvestigationInstrument"}, 
			{value:"INVESTIGATIONPARAMETER", label: "InvestigationParameter"}, 
			{value:"INVESTIGATIONTYPE", label:"InvestigationType"}, 
			{value:"INVESTIGATIONUSER",label:"InvestigationUser"}, 
			{value:"JOB",label:"Job"}, 
			{value:"KEYWORD",label:"Keyword"}, 
			{value:"PARAMETERTYPE",label:"ParameterType"}, 
			{value:"PERMISSIBLESTRINGVALUE", label:"PermissibleStringValue"}, 
			{value:"PUBLICSTEP",label:"PublicStep"}, 
			{value:"PUBLICATION",label:"Publication"}, 
			{value:"REALTEDDATAFILE",label:"RelatedDatafile"}, 
			{value:"RULE",label:"Rule"}, 
			{value:"SAMPLE",label:"Sample"}, 
			{value:"SAMPLEPARAMETER", label:"SampleParameter"}, 
			{value:"SAMPLETYPE",label:"SampleType"}, 
			{value:"SHIFT", label:"Shift"}, 
			{value:"STUDY",label:"Study"}, 
			{value:"STUDYINVESTIGATION", label:"StudyInvestigation"}, 
			{value:"USER",label:"User"},
			{value:"USERGROUP",label:"UserGroup"}

		]

		
	}		   


})();