var rewardCtrl = myMod.controller('RewardCtrl',
					[ '$scope', 'pageTitle', function($scope,pageTitle) {
			//Core function 
			$scope.myData = function() {
				// call to lift function
				var promise = myRTFunctions.doAccProfileRT(); 
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
					var promise = myRTFunctions.doAccProfileRT(); 
					return promise.then(function(data) {
						$scope.$apply(function() {
							$scope.myData = data;
						})
						//add confirmed reward to the page title using injected service
						pageTitle($scope.myData.confirmed_reward)
						return data;
					});
				}, timoutMillis);
			};
			//start the interval function
			$scope.intervalFunction();
			//initial ask for data
			var d = $scope.myData()
			//pageTitle(d.confirmed_reward)
			//console.log(d);
		} ]);
