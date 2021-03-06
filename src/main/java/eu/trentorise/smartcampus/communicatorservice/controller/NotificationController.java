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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.Notifications;
import eu.trentorise.smartcampus.communicatorservice.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.communicatorservice.manager.NotificationManager;
import eu.trentorise.smartcampus.communicatorservice.manager.PermissionManager;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.common.util.Util;
import eu.trentorise.smartcampus.presentation.data.SyncData;
import eu.trentorise.smartcampus.presentation.data.SyncDataRequest;
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

	// Notification by app

	@RequestMapping(method = RequestMethod.GET, value = "/app/public/notification/{capp:.*}")
	public @ResponseBody Notifications getPublicNotificationsByApp(HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestParam("since") Long since,
			@RequestParam("position") Integer position, @RequestParam("count") Integer count, @PathVariable("capp") String capp) throws DataException, IOException, SmartCampusException {

		if (capp == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		Notifications result = new Notifications();
		result.setNotifications(notificationManager.get(null, capp, since, position, count, null));

		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/app/{capp:.*}/notification")
	public @ResponseBody
	Notifications getNotificationsByApp(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@RequestParam("since") Long since,
			@RequestParam("position") Integer position,
			@RequestParam("count") Integer count,
			@PathVariable("capp") String capp) throws DataException,
			IOException, SmartCampusException {

		if (capp == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		
		Notifications result=new Notifications();
		result.setNotifications(notificationManager
				.get(userId, capp, since, position, count, null));

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/app/{capp:.*}/notification/{id}")
	public @ResponseBody
	Notification getNotificationByApp(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id, @PathVariable("capp") String capp)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		if (capp == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}

		return notificationManager.getByIdAndApp(id, capp);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/app/{capp:.*}/notification/{id}")
	public @ResponseBody
	boolean deleteByApp(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id, @PathVariable("capp") String capp)
			throws DataException, IOException, NotFoundException,
			SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.deleteByApp(id, capp);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/app/{capp:.*}/notification/{id}")
	public @ResponseBody
	void updateByApp(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable("id") String id,
			@PathVariable("capp") String capp,
			@RequestBody Notification notification) throws DataException,
			IOException, NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		notificationManager.updateLabelsByApp(id, capp,
				notification.getLabelIds());
		notificationManager.starredByApp(id, capp, notification.isStarred());
	}

	// notification by user

	@RequestMapping(method = RequestMethod.GET, value = "/user/notification")
	public @ResponseBody
	Notifications getNotificationsByUser(HttpServletRequest request,
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
		
		Notifications result=new Notifications();
		result.setNotifications(notificationManager.get(userId, null, since, position, count,
				null));

		return result ;
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

		return notificationManager.getByIdAndUser(id, userId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "user/notification/{id}")
	public @ResponseBody
	boolean deleteByUser(HttpServletRequest request,
			HttpServletResponse response, HttpSession session,
			@PathVariable("id") String id) throws DataException, IOException,
			NotFoundException, SmartCampusException {

		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return notificationManager.deleteByUser(id, userId);
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

		notificationManager.updateLabelsByUser(id, userId,
				notification.getLabelIds());
		notificationManager.starredByUser(id, userId, notification.isStarred());
	}

	@RequestMapping(method = RequestMethod.POST, value = "user/notification/sync")
	public @ResponseBody
	ResponseEntity<SyncData> syncDataByUser(HttpServletRequest request, HttpServletResponse response, @RequestParam long since, @RequestBody Map<String,Object> obj) throws IOException, ClassNotFoundException, DataException  {
		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		SyncDataRequest syncReq = Util.convertRequest(obj, since);
		SyncData out = notificationManager.synchronizeByUser(userId, syncReq.getSyncData());
		return new ResponseEntity<SyncData>(out,HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "app/{capp}/notification/sync")
	public @ResponseBody
	ResponseEntity<SyncData> syncDataByApp(@PathVariable("capp") String capp, HttpServletRequest request, HttpServletResponse response, @RequestParam long since, @RequestBody Map<String,Object> obj) throws IOException, ClassNotFoundException, DataException {
		String userId = getUserId();
		if (userId == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		SyncDataRequest syncReq = Util.convertRequest(obj, since);
		SyncData out = notificationManager.synchronizeByApp(userId, capp, syncReq.getSyncData());
		return new ResponseEntity<SyncData>(out,HttpStatus.OK);
	}
}
