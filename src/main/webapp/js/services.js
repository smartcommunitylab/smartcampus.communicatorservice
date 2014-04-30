angular.module('dev', [ 'ngResource']);

/**
 * App management controller.
 * @param $scope
 * @param $resource
 * @param $http
 * @param $timeout
 */
function CommunicatorController($scope, $resource, $http) {
	// storage
	$scope.storage = {privateKey:{},publicKey:{}};
	// token
	$scope.token = null;
	// appId
	$scope.appId = 'overview';
	// error message
	$scope.error = '';
	// info message
	$scope.info = '';
	
	$scope.notification = {};
	
	// curr parameter name
	$scope.pnamePublic = null;
	// curr parameter value
	$scope.pvaluePublic = null;
	
	// curr parameter name
	$scope.pnamePrivate = null;
	// curr parameter value
	$scope.pvaluePrivate = null;
	
	$scope.notificationTitle= null;
	
	$scope.notificationDescription= null;
	
	$scope.users= null;
	
	
	$scope.fetchRegistration = function() {
		$http(
				{method:'GET',
				 url:'../configuration/app/'+$scope.appId,
				 params:{},
				 headers:{Authorization:'Bearer '+$scope.token}
				})
		.success(function(data) {
			$scope.storage=data;			//{"appId":"sad","privateKey":{"GCM_SENDER_API_KEY":"AIzaSyBA0dQYoF2YQKwm6h5dH4q7h5DTt7LmJrw"},"publicKey":{"GCM_SENDER_ID":"557126495282"}}
			$scope.info = 'Find a old registration with this appid';
			$scope.error = '';
		}).error(function(data) {
			$scope.info = '';
			$scope.error = "No registration found,Start new one";
		});
	};

	$scope.saveConfiguration = function() {
		var method = 'POST';
		
		$scope.storage.appId = $scope.appId
		$http(
				{method:method,
				 url:'../register/app/'+$scope.appId,
				 params:{},
				 data : $scope.storage,
				 headers:{Authorization:'Bearer '+$scope.token}
				})
		.success(function(data) {
			$scope.storage = data;
			$scope.info = 'Registration saved!';
			$scope.error = '';
		}).error(function(data) {
			$scope.info = '';
			$scope.error = "problem storing the configuration";
		});
	
	};
	
	$scope.addPublicConfig = function() {
		var name = $scope.pnamePublic;
		var value = $scope.pvaluePublic;
		if (!name) {
			$scope.error = 'Parameter name cannot be empty!';
			return;
		} else {
			$scope.error = '';
		}
		$scope.info = '';
		if (!$scope.storage) {
			$scope.storage = {};
		}
		if (!$scope.storage.publicKey) {
			$scope.storage.publicKey = [];
		}
		for (var i = 0; i < $scope.storage.publicKey.length; i++) {
			var c = $scope.storage.publicKey[i];
			if (c.name == name) {
				c.value = value;
				$scope.pname = null;$scope.pvalue = null;
				return;
			}
		}
		$scope.storage.publicKey[name]=value;   //.push({name:name,value:value});
		$scope.pname = null;$scope.pvalue = null;
	};

	$scope.removePublicConfig = function(name) {
		for (var i = 0; i < $scope.storage.publicKey.length; i++) {
			var c = $scope.storage.publicKey[i];
			if (c.name == name) {
				$scope.storage.publicKey.splice(i,1);
				return;
			}
		}
	};
	$scope.addPrivateConfig = function() {
		var name = $scope.pnamePrivate;
		var value = $scope.pvaluePrivate;
		if (!name) {
			$scope.error = 'Parameter name cannot be empty!';
			return;
		} else {
			$scope.error = '';
		}
		$scope.info = '';
		if (!$scope.storage) {
			$scope.storage = {};
		}
		if (!$scope.storage.privateKey) {
			$scope.storage.privateKey = [];
		}
		for (var i = 0; i < $scope.storage.privateKey.length; i++) {
			var c = $scope.storage.privateKey[i];
			if (c.name == name) {
				c.value = value;
				$scope.pname = null;$scope.pvalue = null;
				return;
			}
		}
		$scope.storage.privateKey[name]=value;
		$scope.pname = null;$scope.pvalue = null;
	};

	$scope.removePrivateConfig = function(name) {
		for (var i = 0; i < $scope.storage.privateKey.length; i++) {
			var c = $scope.storage.privateKey[i];
			if (c.name == name) {
				$scope.storage.privateKey.splice(i,1);
				return;
			}
		}
	};
	
	$scope.sendNotification=function(){
		var method = 'POST';
		$scope.notification['title']=$scope.notificationTitle;
		$scope.notification['description']=$scope.notificationDescription;
		$scope.notification['author']= {
				appId: $scope.appId,
				userId: null
			};
		
		
		
		$http(
				{method:method,
				 url:'../send/app/'+$scope.appId +"?users="+ $scope.users,
				 params:{},
				 data : $scope.notification,
				 headers:{Authorization:'Bearer '+$scope.token}
				})
		.success(function(data) {
			$scope.info = 'Notification sended!';
			$scope.error = '';
		}).error(function(data) {
			$scope.info = '';
			$scope.error = "problem sending notification";
		});
	};

}