
angular.module('ui.bootstrap').controller('WalletCtrl',['$scope', function($scope){	
	
	$scope.myData = function() {
		//$scope.myData = 
	    var promise = myRTFunctions.doWalletRT(); // call to lift function
	    return promise.then(function(data) {
	      $scope.$apply(function() {
	        $scope.myData = data;
	      })
	      return data;
	    });			
	};
	
	//hooks up a interval function that fetch 
	//the data without user interaction
	var timoutMillis = 1000*60*2;
	$scope.intervalFunction = function() {
		setInterval(function() {
			// call to lift function
			var promise = myRTFunctions.doWalletRT(); 
			return promise.then(function(data) {
				$scope.$apply(function() {
					$scope.myData = data;
				})
				return data;
			});
		}, timoutMillis);
	};
	//kick of the intervall function
	$scope.intervalFunction();	
	
	console.log($scope.myData());
}]);