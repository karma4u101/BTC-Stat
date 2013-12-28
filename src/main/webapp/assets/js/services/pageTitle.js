var pageTitleService = myMod.factory('pageTitle',['$window', function($window) {
	var initTitle = $window.document.title;
	return function(documentTitle){
	  //console.log('pageTitle ='+documentTitle);
	  $window.document.title = '[' + documentTitle+'] '+initTitle;
	}
}]);
