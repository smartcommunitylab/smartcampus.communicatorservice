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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicatorservice.manager.NotificationManager;
import eu.trentorise.smartcampus.communicatorservice.manager.PermissionManager;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.resourceprovider.controller.SCController;
import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;

@Controller
public class NotificationController extends SCController {

	@Autowired
	NotificationManager notificationManager;

	@Autowired
	PermissionManager permissionManager;
	
	@Autowired
	private AuthServices services;
	
	@Override
	protected AuthServices getAuthServices() {
		return services;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/notification")
	public @ResponseBody
	List<Notification> getNotifications(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count) throws DataException,
			IOException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.get(userId,null, since, position, count, null);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/notification/{id}")
	public @ResponseBody
	Notification getNotification(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id) throws DataException, IOException,
			NotFoundException, SmartCampusException {
	
	
		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getById(id);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/notification/{id}")
	public @ResponseBody
	boolean delete(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.deleteById(id);
	}

	

	@RequestMapping(method = RequestMethod.PUT, value = "/notification/{id}")
	public @ResponseBody
	void update(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null ) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabelsById(id, notification.getLabelIds());
		notificationManager.starredById(id, notification.isStarred());
	}
	
	
	//Notification by app
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/app/{capp}/notification")
	public @ResponseBody
	List<Notification> getNotificationsByApp(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count,@PathVariable("capp") String capp) throws DataException,
			IOException, SmartCampusException {

		
		if (capp == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.get(null,capp, since, position, count, null);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/app/{capp}/notification/{id}")
	public @ResponseBody
	Notification getNotificationByApp(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id,@PathVariable("capp") String capp) throws DataException, IOException,
			NotFoundException, SmartCampusException {

		
		if (capp == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getByIdAndApp(id,capp);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/app/{capp}/notification/{id}")
	public @ResponseBody
	boolean deleteByApp(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,@PathVariable("capp") String capp)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.deleteByApp(id,capp);
	}

	

	@RequestMapping(method = RequestMethod.PUT, value = "/app/{capp}/notification/{id}")
	public @ResponseBody
	void updateByApp(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,@PathVariable("capp") String capp,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabelsByApp(id,capp, notification.getLabelIds());
		notificationManager.starredByApp(id,capp, notification.isStarred());
	}
	
	
	
	
	
	//notification by user
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/user/notification")
	public @ResponseBody
	List<Notification> getNotificationsByUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count) throws DataException,
			IOException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.get(userId,null, since, position, count, null);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/user/notification/{id}")
	public @ResponseBody
	Notification getNotificationByUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id) throws DataException, IOException,
			NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getByIdAndUser(id,userId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "user/notification/{id}")
	public @ResponseBody
	boolean deleteByUser(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.deleteByUser(id,userId);
	}

	

	@RequestMapping(method = RequestMethod.PUT, value = "user/notification/{id}")
	public @ResponseBody
	void updateByUser(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabelsByUser(id,userId, notification.getLabelIds());
		notificationManager.starredByUser(id,userId, notification.isStarred());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
