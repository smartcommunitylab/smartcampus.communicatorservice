/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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
 ******************************************************************************/
package eu.trentorise.smartcampus.communicatorservice.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.communicatorservice.manager.CommunicatorManager;
import eu.trentorise.smartcampus.communicatorservice.manager.PreferenceManager;

@Controller
public class PreferenceController extends SCController {

	@Autowired
	PreferenceManager preferenceManager;
	@Autowired
	CommunicatorManager communicatorManager;

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Preference")
	public @ResponseBody
	List<Preference> getPreferenceByUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session)
			throws DataException, IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return Collections.singletonList(communicatorManager.getPreferences(user));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Preference/{id}")
	public @ResponseBody
	Preference getPreference(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable String id) throws DataException, IOException,
			NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		Preference p = communicatorManager.getPreferences(user);

		if (!p.getId().equals(id)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		return p;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.communicator.model.Preference")
	public @ResponseBody
	Preference create(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody Preference pref) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		communicatorManager.storePreferences(user, pref);
		return communicatorManager.getPreferences(user);

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.communicator.model.Preference/{id}")
	public @ResponseBody
	boolean delete(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable String id) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}

		communicatorManager.deleteUser(user);
		return true;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.communicator.model.Preference/{id}")
	public @ResponseBody
	void update(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody Preference pref)
			throws DataException, IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		communicatorManager.storePreferences(user,pref);
	}

}
