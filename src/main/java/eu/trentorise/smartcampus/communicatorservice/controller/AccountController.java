/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.trentorise.smartcampus.communicatorservice.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.AppSignature;
import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.communicator.model.Configuration;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
import eu.trentorise.smartcampus.communicator.model.UserAccount;
import eu.trentorise.smartcampus.communicator.model.UserSignature;
import eu.trentorise.smartcampus.communicatorservice.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.communicatorservice.exceptions.PushException;
import eu.trentorise.smartcampus.communicatorservice.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.communicatorservice.manager.AppAccountManager;
import eu.trentorise.smartcampus.communicatorservice.manager.NotificationManager;
import eu.trentorise.smartcampus.communicatorservice.manager.UserAccountManager;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;

@Controller
public class AccountController extends SCController {

	@Autowired
	UserAccountManager userAccountManager;

	@Autowired
	NotificationManager notificationManager;

	@Autowired
	AppAccountManager appAccountManager;

	@Autowired
	@Value("${gcm.registration.id.key}")
	private String gcm_registration_id_key;

	
	@Autowired
	private AuthServices services;

	@Override
	protected AuthServices getAuthServices() {
		return services;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/conf/registration")
	  public String storageConf() throws SmartCampusException, NotFoundException {
	    return "registration";
	  }

	@RequestMapping(method = RequestMethod.POST, value = "/register/app/{appid:.*}")
	public @ResponseBody
	boolean registerApp(HttpServletRequest request,
			@RequestBody AppSignature signature, @PathVariable String appid,
			HttpSession session) throws DataException, IOException,
			NotFoundException, SmartCampusException, AlreadyExistException {

	//	String senderId = signature.getSenderId();
	//	String apikey = signature.getApiKey();
	//	String appId = signature.getAppId();

		List<Configuration> listConf = new ArrayList<Configuration>();

		// set value of sender/serverside app registration code
		if (signature.getPrivateKey() == null)
			throw new NotFoundException();
		// if app is not registered?use ours?

		
//		listvalue.put(gcm_sender_key, apikey);
//		listvalue.put(gcm_sender_id, senderId);
//		
//		listvalue.put(app_id, appid);
//
		Configuration e = new Configuration(CloudToPushType.GOOGLE, signature.getPrivateKey(),signature.getPublicKey());

		listConf.add(e);

		AppAccount appAccount;
		List<AppAccount> listApp = appAccountManager.getAppAccounts(appid);
		if (listApp.isEmpty()) {
			appAccount = new AppAccount();
			appAccount.setAppId(appid);
			appAccount.setConfigurations(listConf);
			appAccountManager.save(appAccount);
		} else {
			appAccount = listApp.get(0);
			appAccount.setConfigurations(listConf);
			appAccountManager.update(appAccount);
		}

		return true;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/register/user/{appid:.*}")
	public @ResponseBody
	boolean registerUserToPush(HttpServletRequest request,
			@PathVariable String appid, @RequestBody UserSignature signature,
			HttpSession session) throws DataException, IOException,
			NotFoundException, SmartCampusException, AlreadyExistException {

		String userId = getUserId();
		return registerUser(appid, signature, userId);

	}
	@RequestMapping(method = RequestMethod.POST, value = "/register/appuser/{appid}/{user}")
	public @ResponseBody
	boolean registerAppUserToPush(HttpServletRequest request,
			@PathVariable String appid, @PathVariable String user, @RequestBody UserSignature signature,
			HttpSession session) throws DataException, IOException,
			NotFoundException, SmartCampusException, AlreadyExistException {

		String userId = user;
		String tokenUserId = getUserId();
		if (tokenUserId != null && ! tokenUserId.equals(userId)) {
			throw new IllegalArgumentException("Cannot register arbitrary user within a user flow.");				
		}

		return registerUser(appid, signature, userId);

	}

	private boolean registerUser(String appid, UserSignature signature,
			String userId) throws AlreadyExistException,
			JsonGenerationException, JsonMappingException, IOException {
		UserAccount userAccount;
		String registrationId = signature.getRegistrationId();
		// set value of sender/serverside user registration code
		if (registrationId == null) {
			throw new IllegalArgumentException("Missing registration id.");
		}

		String appName = signature.getAppName();

		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
				userId, appName);

		if (listUser.isEmpty()) {
			userAccount = new UserAccount();
			userAccount.setAppId(appid);
			userAccount.setUserId(userId);
			userAccountManager.save(userAccount);
		} else {
			userAccount = listUser.get(0);
		}

		List<Configuration> listConf = userAccount.getConfigurations();
		if (listConf == null) {
			listConf = new ArrayList<Configuration>();
		}
		boolean exists = false;
		for (Configuration c : listConf) {
			if (registrationId.equals(c.get(gcm_registration_id_key))) {
				exists = true; 
				break;
			}
		}

		// TODO ask type of device
		if (!exists) {
			Map<String, String> listvalue = new HashMap<String, String>();
			listvalue.put(gcm_registration_id_key, registrationId);

			Configuration e = new Configuration(CloudToPushType.GOOGLE, listvalue);
			listConf.add(e);

			userAccount.setConfigurations(listConf);
			userAccountManager.update(userAccount);
		}

		return true;
	}
	@RequestMapping(method = RequestMethod.DELETE, value = "/unregister/user/{appid:.*}")
	public @ResponseBody
	boolean unregisterUserToPush(HttpServletRequest request,
			@PathVariable String appid, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		String userId = getUserId();
		return unregisterUser(appid, userId);

	}
	@RequestMapping(method = RequestMethod.DELETE, value = "/unregister/appuser/{appid}/{user}")
	public @ResponseBody
	boolean unregisterAPpUserToPush(HttpServletRequest request,
			@PathVariable String appid, @PathVariable String user, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		String userId = user;
		String tokenUserId = getUserId();
		if (tokenUserId != null && ! tokenUserId.equals(userId)) {
			throw new IllegalArgumentException("Cannot register arbitrary user within a user flow.");				
		}
		return unregisterUser(appid, userId);

	}

	private boolean unregisterUser(String appid, String userId) {
		UserAccount userAccount;

		List<UserAccount> listUser = userAccountManager.findByUserIdAndAppName(
				userId, appid);

		if (!listUser.isEmpty()) {
			userAccount = listUser.get(0);

			userAccount.setConfigurations(null);
			userAccountManager.update(userAccount);

		}

		return true;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/unregister/app/{appid:.*}")
	public @ResponseBody
	boolean unregisterAppToPush(HttpServletRequest request,
			@PathVariable String appid, HttpSession session)
			throws DataException, IOException, NotFoundException,
			SmartCampusException, AlreadyExistException {

		
		

		AppAccount listUser = appAccountManager.getAppAccount(appid);
				

		if (listUser!=null) {
			

			listUser.setConfigurations(null);
			appAccountManager.delete(appid);

		}

		return true;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/send/app/{appId:.*}")
	public @ResponseBody void sendAppNotification(HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam(value = "users", required = false) String[] userIds,
			@RequestBody Notification notification, @PathVariable("appId") String appId) throws DataException, IOException, NotFoundException, PushException {

		NotificationAuthor author = new NotificationAuthor();
		author.setAppId(appId);

		notification.setType(appId);
		notification.setAuthor(author);

		if (userIds == null || userIds.length == 0) {
			notification.setId(null);
			notification.setUser(null);
			notification.addChannelId(appId);
			notificationManager.create(notification);
		} else {
			for (String receiver : userIds) {
				notification.setId(null);
				notification.setUser(receiver);
				notificationManager.create(notification);
			}
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/send/user")
	public @ResponseBody
	void sendUserNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam(value = "users", required = true) String[] userIds,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, PushException {

		String userId = getUserId();
//		NotificationAuthor author = new NotificationAuthor();
//		author.setUserId(userId);

		notification.setType(userId);

		for (String receiver : userIds) {
			notification.setId(null);
			notification.setUser(receiver);
//			notification.setAuthor(author);
			notificationManager.create(notification);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/configuration/app/{appid:.*}")
	public @ResponseBody
	AppSignature requestAppConfigurationToPush(
			HttpServletRequest request, @PathVariable String appid,
			HttpSession session) throws DataException, IOException,
			NotFoundException, SmartCampusException, AlreadyExistException {

		Map<String, String> publicList = new HashMap<String, String>();
		Map<String, String> privateList = new HashMap<String, String>();
		AppAccount index = appAccountManager.getAppAccount(appid);

		if (index != null && index.getConfigurations()!= null && !index.getConfigurations().isEmpty()) {
			for (Configuration x : index.getConfigurations()) {
				privateList.putAll(x.getPrivateKey());
				publicList.putAll(x.getPublicKey());
			}
		}

		AppSignature appSignature=new AppSignature();
		appSignature.setAppId(appid);
		appSignature.setPrivateKey(privateList);
		appSignature.setPublicKey(publicList);
		
		return appSignature;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/configuration/user/{appid:.*}")
	public @ResponseBody
	Map<String, String> requestUserConfigurationToPush(
			HttpServletRequest request, @PathVariable String appid,
			HttpSession session) throws DataException, IOException,
			NotFoundException, SmartCampusException, AlreadyExistException {

		Map<String, String> result = new HashMap<String, String>();

		String userid = getUserId();
		List<UserAccount> list = userAccountManager.findByUserIdAndAppName(
				userid, appid);
		for (UserAccount index : list) {
			if (index != null && !index.getConfigurations().isEmpty()) {
				for (Configuration x : index.getConfigurations()) {
					result.putAll(x.getPrivateKey());
					if(x.getPublicKey()!=null)
					result.putAll(x.getPublicKey());
				}
			}

		}
		return result;

	}
	
		@RequestMapping(method = RequestMethod.GET, value = "/configuration/public/{appid:.*}")
		public @ResponseBody
		Map<String, String> requestPublicAppConfigurationToPush(
				HttpServletRequest request, @PathVariable String appid,
				HttpSession session) throws DataException, IOException,
				NotFoundException, SmartCampusException, AlreadyExistException {

			Map<String, String> result = new HashMap<String, String>();
			AppAccount index = appAccountManager.getAppAccount(appid);

			if (index != null && index.getConfigurations()!= null && !index.getConfigurations().isEmpty()) {
				for (Configuration x : index.getConfigurations()) {
					result.putAll(x.getPublicKey());
				}
			}

			return result;

		}

}
