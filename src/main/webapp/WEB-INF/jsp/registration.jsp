<!DOCTYPE html>
<html lang="en" ng-app="dev">
  <head>
    <meta charset="utf-8">
    <title>SmartCampus Developers</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <link href="../css/bs-ext.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
    <link href="../css/bootstrap-responsive.min.css" rel="stylesheet">

    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular-resource.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.7/angular-cookies.min.js"></script>
    <script src="../lib/jquery.js"></script>
    <script src="../lib/bootstrap.min.js"></script>
    <script src="../js/services.js"></script>
  </head>

  <body>
    <div class="container" ng-controller="CommunicatorController">
		    <div class="alert alert-error" ng-show="error != ''">{{error}}</div>
		    <div class="alert alert-success" ng-show="info != ''">{{info}}</div>
        <form ng-submit="fetchRegistration()">
          <fieldset>    
              <legend>App Registration </legend>
               <label>Client token (generate it through the Developer Console)<label>
               <input type="text" ng-model="token" placeholder="Client credentials flow token"  class="input-xxlarge">

               <label>App ID parameter<label>
               <input type="text" ng-model="appId" placeholder="appId"  class="input-xlarge">                       
            <div class="row-fluid" >
                 <button type="submit" class="btn btn-primary">Find last registration</button>
               </div>
           </fieldset>
        </form>
        <form ng-submit="saveConfiguration()">
            <fieldset>                 
               <label>Add PUBLIC configuration parameter</label>
               <p style="font-size: 8px;color: #666"> (For the Push Service you must set GCM_SENDER_API_KEY and GCM_SENDER_ID with Google API value)</p>
          <br/>
 							<div class="row-fluid">
						    <div class="span7">
							    <input type="text" ng-model="pnamePublic" placeholder="parameter key" class="input-xlarge">
							  	<input type="text" ng-model="pvaluePublic" placeholder="parameter value" class="input-xlarge">
							  </div>	
							  <div class="span1"><a class="btn btn-mini btn-primary" href="#" ng-click="addPublicConfig()"><i class="icon-plus icon-white"></i></a></div>
						  </div>
					  	<hr class="hr-min"/>
						  <div ng-repeat="(key, value) in storage.publicKey">
							  <div class="row-fluid" >
								 <div class="span3">{{key}}</div>
								 <div class="span4">{{value}}</div>
								 <div class="span1"><a class="btn btn-mini" href="#" ng-click="removePublicConfig(publicConfig.name)"><i class="icon-minus"></i></a></div>
							  </div>
						  </div>	
						  <hr class="hr-min"/>
						  
						  
						   <label>Add PRIVATE configuration parameter</label>
 							<div class="row-fluid">
						    <div class="span7">
							    <input type="text" ng-model="pnamePrivate" placeholder="parameter key" class="input-xlarge">
							  	<input type="text" ng-model="pvaluePrivate" placeholder="parameter value" class="input-xlarge">
							  </div>	
							  <div class="span1"><a class="btn btn-mini btn-primary" href="#" ng-click="addPrivateConfig()"><i class="icon-plus icon-white"></i></a></div>
						  </div>
					  	<hr class="hr-min"/>
						  <div ng-repeat="(key, value) in storage.privateKey">
							  <div class="row-fluid" >
								 <div class="span3">{{key}}</div>
								 <div class="span4">{{value}}</div>
								 <div class="span1"><a class="btn btn-mini" href="#" ng-click="removePrivateConfig(privateConfig.name)"><i class="icon-minus"></i></a></div>
							  </div>
						  </div>	
						  <hr class="hr-min"/>
						  <button type="submit" class="btn btn-primary">Save configuration</button>
           </fieldset>
        </form>
         <form ng-submit="sendNotification()">
          <fieldset>    
              <legend>Send Notification </legend>
               <label>Title<label>
               <input type="text" ng-model="notificationTitle" placeholder="title"  class="input-xxlarge">
                <label>Description<label>
               <input type="text" ng-model="notificationDescription" placeholder="description"  class="input-xxlarge">

               <label>Users<label>
               <input type="text" ng-model="users" placeholder="1,21,3,..."  class="input-xlarge">                       
            <div class="row-fluid" >
                 <button type="submit" class="btn btn-primary">Send Notification </button>
               </div>
           </fieldset>           
       
        </form>
    </div>
  </body>
</html>
