package eu.trentorise.smartcampus.communicatorservice.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.ListAppAccount;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.NotFoundException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.communicatorservice.manager.AppAccountManager;

@Controller
public class AppAccountController extends SCController {

	

	@Autowired
	private AppAccountManager appAccountManager;

	@RequestMapping(method = RequestMethod.POST, value = "/appaccount")
	public @ResponseBody
	AppAccount create(HttpServletRequest request,
			@RequestBody AppAccount appAccount) throws SmartCampusException,
			AlreadyExistException {

		return appAccountManager.save(appAccount);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/appaccount/{appName}")
	public @ResponseBody
	AppAccount update(HttpServletRequest request,
			@RequestBody AppAccount appAccount, @PathVariable String appName)
			throws SmartCampusException, NotFoundException {
		return appAccountManager.update(appAccount);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/appaccount/{appName}/{appAccountId}")
	public @ResponseBody
	boolean delete(HttpServletRequest request, @PathVariable String appName,
			@PathVariable String appAccountId) throws SmartCampusException {
		appAccountManager.delete(appAccountId);
		return true;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/appaccount/{appName}")
	public @ResponseBody
	ListAppAccount getAppAccounts(HttpServletRequest request,
			@PathVariable String appName) throws SmartCampusException {
		ListAppAccount result = new ListAppAccount();
		result.setAppAccounts(appAccountManager.getAppAccounts(appName));
		return result;
	}

}
