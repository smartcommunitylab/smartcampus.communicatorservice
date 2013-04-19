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

package eu.trentorise.smartcampus.vas.communicator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;

import eu.trentorise.smartcampus.vas.communicator.manager.PermissionManager;
import eu.trentorise.smartcampus.vas.communicator.manager.UserAccountManager;

import eu.trentorise.smartcampus.communicator.model.ListUserAccount;

import eu.trentorise.smartcampus.exceptions.*;
import eu.trentorise.smartcampus.communicator.model.UserAccount;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;

@Controller
public class UserAccountController extends SCController {

	@Autowired
	UserAccountManager accountManager;

	@Autowired
	PermissionManager permissionManager;

	@RequestMapping(method = RequestMethod.POST, value = "/useraccount/{appName}")
	public @ResponseBody
	UserAccount save(HttpServletRequest request,
			@RequestBody UserAccount account, @PathVariable String appName)
			throws  AlreadyExistException, SmartCampusException {
		User user = retrieveUser(request);

		// if userId isn't setted, it will be use the authToken to retrieve it
		if (account.getUserId() <= 0) {
			account.setUserId(user.getId());
		}
		if (!permissionManager.checkAccountPermission(user, account)) {
			throw new SecurityException();
		}
		return accountManager.save(account);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/useraccount/{appName}/{aid}")
	public @ResponseBody
	void update(HttpServletRequest request, @RequestBody UserAccount account,
			@PathVariable String appName, @PathVariable("aid") String aid)
			throws SmartCampusException {
		User user = retrieveUser(request);

		if (account.getId() == null) {
			account.setId(aid);
		}

		if (!permissionManager.checkAccountPermission(user, account)) {
			throw new SecurityException();
		}

		accountManager.update(account);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/useraccount/{appName}/{aid}")
	public @ResponseBody
	void delete(HttpServletRequest request, @PathVariable String appName,
			@PathVariable("aid") String aid) throws
			NotFoundException, SmartCampusException {
		User user = retrieveUser(request);

		if (!permissionManager.checkAccountPermission(user, aid)) {
			throw new SecurityException();
		}
		accountManager.delete(aid);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/useraccount/{appName}/{aid}")
	public @ResponseBody
	UserAccount getAccountById(HttpServletRequest request,
			@PathVariable String appName, @PathVariable String aid)
			throws NotFoundException, SmartCampusException {
		//User user = retrieveUser(request);
		return accountManager.findById(aid);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/useraccount/{appName}")
	public @ResponseBody
	ListUserAccount getAccounts(HttpServletRequest request,
			@PathVariable String appName) throws SmartCampusException {
		ListUserAccount result = new ListUserAccount();
		result.setUserAccounts(accountManager.findUserAccounts(appName));
		return result;
	}
}
