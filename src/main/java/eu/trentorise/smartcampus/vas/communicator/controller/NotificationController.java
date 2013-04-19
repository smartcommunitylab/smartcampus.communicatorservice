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
package eu.trentorise.smartcampus.vas.communicator.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
import eu.trentorise.smartcampus.controllers.SCController;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;
import eu.trentorise.smartcampus.vas.communicator.manager.NotificationManager;
import eu.trentorise.smartcampus.vas.communicator.manager.PermissionManager;

@Controller
public class NotificationController extends SCController {

	@Autowired
	NotificationManager notificationManager;

	@Autowired
	PermissionManager permissionManager;

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Notification")
	public @ResponseBody
	List<Notification> getNotifications(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count) throws DataException,
			IOException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.get(user, since, position, count, null);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	Notification getNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id) throws DataException, IOException,
			NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getById(id);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	boolean delete(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.delete(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/eu.trentorise.smartcampus.communicator.model.Notification")
	public @ResponseBody
	void create(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestBody Notification notification)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		// attenzione non sempre utente puo' avere il servizio

		notificationManager.create(notification);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/eu.trentorise.smartcampus.communicator.model.Notification/{id}")
	public @ResponseBody
	void update(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabels(id, notification.getLabelIds());
		notificationManager.starred(id, notification.isStarred());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/objects")
	public @ResponseBody
	Map<String, List<Notification>> filterNotifications(
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @RequestParam("filter") String jsonFilter,
			@RequestParam Long since, @RequestParam Integer position,
			@RequestParam Integer count) throws IOException, DataException,
			SmartCampusException {

		NotificationFilter filter = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			filter = mapper.readValue(jsonFilter, NotificationFilter.class);
		} catch (JsonMappingException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		Map<String, List<Notification>> result = new HashMap<String, List<Notification>>();
		List<Notification> notList = notificationManager.get(user, since,
				position, count, filter);

		result.put(Notification.class.getName(),
				(notList == null) ? Collections.<Notification> emptyList()
						: notList);
		return result;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/send/app/{appId}")
	public @ResponseBody
	void sendAppNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody Notification notification,
			@PathVariable("appId") String appId) throws DataException,
			IOException, NotFoundException {

		String usersParam = request.getParameter("users");
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		List<String> users = mapper.readValue(usersParam, List.class);

		notification.setType(appId);

		for (String receiver : users) {
			notification.setId(null);
			notification.setUser(receiver);
			notificationManager.create(notification);
		}
	}

	/*@RequestMapping(method = RequestMethod.POST, value = "/send/user")
	public @ResponseBody
	void sendUserNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		User user = retrieveUser(request);
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		String usersParam = request.getParameter("users");
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		List<String> users = mapper.readValue(usersParam, List.class);

		NotificationAuthor author = new NotificationAuthor();
		author.setSocialId(user.getSocialId());
		notification.setAuthor(author);
		notification.setType("user");

		for (String receiver : users) {
			notification.setId(null);
			notification.setUser(receiver);
			notificationManager.create(notification);
		}

	}

	*/

}
