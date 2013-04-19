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
package eu.trentorise.smartcampus.vas.communicator.manager;

import it.sayservice.platform.client.DomainEngineClient;
import it.sayservice.platform.client.DomainObject;
import it.sayservice.platform.client.InvocationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.Channel;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.data.BasicObject;
import eu.trentorise.smartcampus.presentation.data.SyncData;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;
import eu.trentorise.smartcampus.vas.communicator.storage.CommunicatorStorage;
import eu.trentorise.smartcampus.vas.communicator.util.MailNotificationSender;
import eu.trentorise.smartcampus.vas.communicator.util.NotificationsUtil;

@Component
public class CommunicatorManager {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	CommunicatorStorage storage;
	//@Autowired
	DomainEngineClient domainClient;
	@Autowired
	MailNotificationSender sender;
	
	// check feed updates within last two days
	private static final long OLD_FEED_MESSAGES_INTERVAL = 1000*60*60*24*2L;
	
	private Preference ensureUserData(User user) throws DataException {
		Preference userPrefs = null;
		try {
			userPrefs = getByUser(Utils.userId(user));
		} catch (NotFoundException e) {
			userPrefs = new Preference();
			userPrefs.setUser(Utils.userId(user));
			createDefaultSources(user);
			userPrefs.setId(userPrefs.getUser());
			storage.storeObject(userPrefs);
		}
		return userPrefs;
	}
	private void createDefaultSources(User user) throws DataException {
		List<String> factories = null;
		try {
			factories = domainClient.searchDomainObjects("eu.trentorise.smartcampus.domain.communicator.AbstractSourceFactory", null);
		} catch (InvocationException e1) {
			logger.error("Failed to access factories: "+e1.getMessage());
			throw new DataException("Failed to access factories");
		}
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("userId", Utils.userId(user));
		parameters.put("userSocialId", ""+user.getSocialId());

		if (factories != null) {
			for (String f : factories) {
				try {
					DomainObject o = new DomainObject(f);
					domainClient.invokeDomainOperation("createDefaultSource", o.getType(), o.getId(), parameters, null, null);
					Channel channel = new Channel();
					channel.setUserId(user.getId());
					channel.setUser(Utils.userId(user));
					channel.setFeed(false);
					channel.setSourceType((String)o.getContent().get("sourceType"));
					channel.setTitle((String)o.getContent().get("name"));
					storage.storeObject(channel);
				} catch (Exception e1) {
					logger.error("Default source not created for factory "+f+": "+e1.getMessage());
					continue;
				}
			}
		}

	}
	private Preference getByUser(String user) throws NotFoundException, DataException {
		List<Preference> list = storage.getObjectsByType(Preference.class, user);
		if (list == null || list.size() == 0) {
			throw new NotFoundException();
		} else if (list.size() > 1) {
			throw new DataException("More than one preference object for user "+user);
		}
		return list.get(0);
	}

	
	public Preference getPreferences(User user) throws DataException {
		return ensureUserData(user);
	}

	public void storePreferences(User user, Preference preference) throws DataException {
		ensureUserData(user);
		preference.setUser(Utils.userId(user));
		storage.storeObject(preference);
	}

	public void storeChannel(User user, Channel channel) throws DataException {
		channel.setUser(Utils.userId(user));
		channel.setUserId(user.getId());

		/*
		 * In case of new feed channel, apply the filter to the buffered feed messages
		 */
			try {
				storage.getObjectById(channel.getId(), Channel.class);
			} catch (NotFoundException e) {
				if (channel.isFeed()) {
					applyNewFeedChannelToOldMessages(user, channel);
				} else {
					applyNewSourceChannelToOldMessages(user, channel);
				}
			}
		storage.storeObject(channel);
	}
	private void applyNewSourceChannelToOldMessages(User user, Channel channel) throws DataException {
		NotificationFilter filter = new NotificationFilter();
		filter.setSourceType(channel.getSourceType());
		List<Notification> notifications = storage.searchNotifications(Utils.userId(user), 0L, 0, -1, filter);
		Set<String> toSend = new HashSet<String>();
		if (notifications != null) {
			for (Notification n : notifications) {
				if (channel.applies(n)) {
					n.addChannelId(channel.getId());
					if (channel.getLabelIds() != null){
						if (n.getLabelIds() == null) n.setLabelIds(channel.getLabelIds());
						else n.getLabelIds().addAll(channel.getLabelIds());
					}							
					if (!NotificationsUtil.applyChannelActions(channel, n, toSend)) continue;
					storage.storeObject(n);
				}
			}
		}
	}
	private void applyNewFeedChannelToOldMessages(User user, Channel channel) throws DataException {
		// channel is new
		long since = 0L;
		NotificationFilter filter = new NotificationFilter();
		filter.setSourceType(channel.getSourceType());
		List<Notification> notifications = storage.searchNotifications(null, since, 0, -1, filter);
		Set<String> toSend = new HashSet<String>();
//				List<Notification> matching = new ArrayList<Notification>();
		if (notifications != null) {
			long now = System.currentTimeMillis();
			for (Notification n : notifications) {
				if (n.getTimestamp() > 0 && (now - n.getTimestamp())>OLD_FEED_MESSAGES_INTERVAL) continue;
				if (channel.applies(n)) {
					Notification newNotification = null;
					String copyId = Notification.userCopyId(n.getId(), Utils.userId(user));
					try {
						newNotification = storage.getObjectById(copyId, Notification.class);
					} catch (NotFoundException e1) {
						newNotification = n.copy(Utils.userId(user));
						newNotification.setUser(Utils.userId(user));
					}
					
					newNotification.addChannelId(channel.getId());
					if (channel.getLabelIds() != null){
						if (newNotification.getLabelIds() == null) newNotification.setLabelIds(channel.getLabelIds());
						else newNotification.getLabelIds().addAll(channel.getLabelIds());
					}							
					if (!NotificationsUtil.applyChannelActions(channel, newNotification, toSend)) continue;
					storage.storeObject(newNotification);
//							matching.add(newNotification);
				}
			}
		}
//				if (matching != null && toSend != null) {
//					Map<String,List<Notification>> map = new HashMap<String, List<Notification>>();
//					for (String s : toSend) {
//						map.put(s, matching);
//					}
//					NotificationsUtil.sendEmail(map, sender);
//				}
	}
	
	private void deleteChannel(String id) throws DataException {
		try {
			storage.deleteObject(storage.getObjectById(id, Channel.class));
		} catch (Exception e) {
			logger.error("Problem deleting channel "+id+": "+e.getMessage());
		}
	}
	public SyncData synchronize(User user, SyncData input) throws DataException {
		ensureUserData(user);
		SyncData output = storage.getSyncData(input.getVersion(), Utils.userId(user), true);
		if (input.getDeleted() != null) {
			for (String s : input.getDeleted().keySet()) {
				if (s.equals(Channel.class.getName())) {
					for (String id : input.getDeleted().get(s)) {
						deleteChannel(id);
					}
				} 
			}
		}
		if (input.getUpdated() != null) {
			for (String s : input.getUpdated().keySet()) {
				for (BasicObject o : input.getUpdated().get(s)) {
					o.setUser(Utils.userId(user));
					if (s.equals(Channel.class.getName())) {
						Channel f = (Channel)o;
						storeChannel(user, f);
					} 
					else if (s.equals(Preference.class.getName())) {
						List<Preference> list = storage.getObjectsByType(Preference.class, Utils.userId(user));
						if (list != null) {
							for (Preference p : list) storage.deleteObjectPermanently(p);
						}
					}
				} 
			}
		}
		// check new message created/updated with new/updated channels
		SyncData newOutput = storage.getSyncData(output.getVersion(), Utils.userId(user), true);
		if (newOutput != null) {
			merge(output,newOutput);
		}

		storage.cleanSyncData(input, Utils.userId(user));
		return output;
	}
	
	private void merge(SyncData output, SyncData newOutput) {
		output.setVersion(newOutput.getVersion());
		if (newOutput.getDeleted() != null) {
			if (output.getDeleted() == null) output.setDeleted(new HashMap<String, List<String>>());
			for (String key : newOutput.getDeleted().keySet()) {
				if (output.getDeleted().get(key) != null) output.getDeleted().get(key).addAll(newOutput.getDeleted().get(key));
				else output.getDeleted().put(key,newOutput.getDeleted().get(key));
 			}
		}
		
		if (newOutput.getUpdated() != null) {
			if (output.getUpdated() == null) output.setUpdated(new HashMap<String, List<BasicObject>>());
			for (String key : newOutput.getUpdated().keySet()) {
				if (output.getUpdated().get(key) != null) output.getUpdated().get(key).addAll(newOutput.getUpdated().get(key));
				else output.getUpdated().put(key,newOutput.getUpdated().get(key));
 			}
		}

	}
	public void deleteUser(User user) throws DataException {
		storage.deleteObjectsPermanently(Notification.class, Utils.userId(user));
		storage.deleteObjectsPermanently(Channel.class, Utils.userId(user));
		storage.deleteObjectPermanently(getPreferences(user));
	}
}
