//angular.module('ui.bootstrap').controller('KapitonCtrl',['$scope', function($scope){	
var kapitonCtrl = myMod.controller('LoginCtrl',['$scope','$window', function($scope,$window){	
	
	$scope.init = {}
	$scope.myData = {loggedIn:false}
	$scope.login = function(user) {
		var json = angular.toJson($scope.user);
	    var promise = myRTFunctions.doLoginRT(json); // call to lift function
	    return promise.then(function(data) {
	      $scope.$apply(function() {	
	        $scope.myData = data;
	        $scope.checkStatus($scope.myData.loggedIn)
	      })
	      return data;
	    });			
	};

	$scope.checkStatus = function(loggedIn){
	  if(loggedIn){
		  $window.location.reload();
	  }
	}
	$scope.reset = function(){
		$scope.user = angular.copy($scope.init);
	}

	$scope.reset();
}]);