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
package eu.trentorise.smartcampus.communicatorservice.manager;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.PushException;
import eu.trentorise.smartcampus.communicatorservice.filter.NotificationFilter;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl.ApplePushNotificationServiceManager;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl.GoogleCloudMessengerManager;
import eu.trentorise.smartcampus.communicatorservice.storage.CommunicatorStorage;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.data.SyncData;

@Component
public class NotificationManager {
	
	/**
	 * 
	 */
	private static final int MAX_CACHE_SIZE = 1000;

	private static final Logger logger = Logger
			.getLogger(NotificationManager.class);

	@Autowired
	CommunicatorStorage storage;

	@Autowired
	AppAccountManager appAccountManager;

	@Autowired
	GoogleCloudMessengerManager googleManager;

	
	/**
	 * Take last week of notifications
	 */
	private CacheLoader<String, NotificationCache> loader = new CacheLoader<String, NotificationCache>() {

		@Override
		public NotificationCache load(String key) throws Exception {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -7);
			Long since = c.getTimeInMillis();
			List<Notification> list = storage.searchNotifications(null, key, since, 0, MAX_CACHE_SIZE, null);
			if (list == null) list = Collections.emptyList();
			return new NotificationCache(list, since);
		}
		
	};

	private LoadingCache<String, NotificationCache> notificationCache =  CacheBuilder.newBuilder()
		       .maximumSize(100)
		       .expireAfterWrite(1, TimeUnit.DAYS)
		       .build(loader);
	


	@Autowired
	ApplePushNotificationServiceManager appleManager;

	public void create(Notification notification) throws NotFoundException, DataException, PushException {
		if (notification.getAuthor() == null)
			throw new DataException("No Author in this notification");

		notification.setUpdateTime(System.currentTimeMillis());
		notification.setTimestamp(System.currentTimeMillis());
		storage.storeObject(notification);
		
		if(notification.getAuthor().getAppId()!=null){
			// app notification, refresh the cache
			if (notification.getUser() == null) {
				notificationCache.refresh(notification.getAuthor().getAppId());
			}
			
			List<AppAccount> listApp = appAccountManager
					.getAppAccounts(notification.getAuthor().getAppId());
			// if app registered before it can send,otherwise doesn't send in
			// push
			if (!listApp.isEmpty()) {
				// check the first appAccount
				AppAccount appAccount = listApp.get(0);
				// get the account could type
				
				List<CloudToPushType> listType = appAccount
						.getCloudToPushTypeConfigured();
				
					
				

				// send on all the cloud if a single app has more account
				for (CloudToPushType cpt : listType) {
					PushServiceCloud servicePush = null;

					switch (cpt) {
					case GOOGLE: {
						servicePush = googleManager;
						break;
					}
					case APPLE: {
						servicePush = appleManager;
						break;
					}
					case WINDOWSPHONE: {
						break;
					}

					}

					try {
						servicePush.sendToCloud(notification);
					} catch (NoUserAccount e) {
//						e.printStackTrace();
					}
				}
			}else{
				logger.warn("The app account "+ notification.getAuthor().getAppId() +" not yet registered");
				//throw new NotFoundException("App account not registered");
			}
		}
		
	}

	public boolean deleteByApp(String id, String capp)
			throws NotFoundException, DataException {
		storage.deleteObject(storage.getObjectByIdAndApp(id, capp,
				Notification.class));
		return true;
	}

	public boolean deleteByUser(String id, String userId)
			throws NotFoundException, DataException {
		storage.deleteObject(storage.getObjectByIdAndUser(id, userId,
				Notification.class));
		return true;
	}

	public boolean deleteById(String id) throws NotFoundException,
			DataException {
		storage.deleteObject(storage.getObjectById(id, Notification.class));
		return true;
	}

	public List<Notification> get(String userId, String capp, Long since, Integer position, Integer count, NotificationFilter filter) throws DataException {
		if (userId == null) {
			try {
				if (since == null) since = System.currentTimeMillis();
				NotificationCache cached = notificationCache.get(capp);
				int pos = position != null ? position : 0;
				int c = count != null ? count : 100;
				
				int sublistSize = 0;
				for (Notification n : cached.notifications) {
					if (n.getTimestamp() >= since) sublistSize++;
					else break;
				}
				if (pos >= sublistSize) return Collections.emptyList();
				
				if (cached.since <= since && pos + c < MAX_CACHE_SIZE) {
					int max = (pos + c) > sublistSize ? sublistSize : (pos + c); 
					return cached.notifications.subList(pos, max);
				}
			} catch (ExecutionException e) {
				logger.error("Error reading cached data: "+ e.getMessage(), e);
			}
		}
		return storage.searchNotifications(userId, capp, since, position, count, filter);
	}

	public Notification getById(String id) throws NotFoundException,
			DataException {
		return storage.getObjectById(id, Notification.class);
	}

	public Notification getByIdAndApp(String id, String capp)
			throws NotFoundException, DataException {
		return storage.getObjectByIdAndApp(id, capp, Notification.class);
	}

	public Notification getByIdAndUser(String id, String userId)
			throws NotFoundException, DataException {
		return storage.getObjectByIdAndUser(id, userId, Notification.class);
	}

	/**
	 * set starred value to given value
	 * 
	 * @param id
	 * @param starredStatus
	 * @throws NotFoundException
	 * @throws DataException
	 */
	public void starredByApp(String id, String capp, boolean starredStatus)
			throws NotFoundException, DataException {
		changeStarredStatusByApp(id, capp, starredStatus);
	}

	public void starredByUser(String id, String userid, boolean starredStatus)
			throws NotFoundException, DataException {
		changeStarredStatusByUser(id, userid, starredStatus);
	}

	public void starredById(String id, boolean starredStatus)
			throws NotFoundException, DataException {
		changeStarredStatusById(id, starredStatus);
	}

	/**
	 * set starred value to true
	 * 
	 * @param id
	 *            notification id
	 * @throws NotFoundException
	 * @throws DataException
	 */
	public void starredByApp(String id, String capp) throws NotFoundException,
			DataException {
		changeStarredStatusByApp(id, capp, true);
	}

	public void starredByUser(String id, String userId)
			throws NotFoundException, DataException {
		changeStarredStatusByUser(id, userId, true);
	}

	private void changeStarredStatusByUser(String id, String userId,
			boolean starred) throws NotFoundException, DataException {
		Notification notification = storage.getObjectByIdAndUser(id, userId,
				Notification.class);
		notification.setStarred(starred);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	private void changeStarredStatusByApp(String id, String capp,
			boolean starred) throws NotFoundException, DataException {
		Notification notification = storage.getObjectByIdAndApp(id, capp,
				Notification.class);
		notification.setStarred(starred);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	private void changeStarredStatusById(String id, boolean starred)
			throws NotFoundException, DataException {
		Notification notification = storage.getObjectById(id,
				Notification.class);
		notification.setStarred(starred);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	public void updateLabelsByApp(String id, String capp, List<String> labelIds)
			throws NotFoundException, DataException {
		Notification notification = storage.getObjectByIdAndApp(id, capp,
				Notification.class);
		notification.setLabelIds(labelIds);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	public void updateLabelsById(String id, List<String> labelIds)
			throws NotFoundException, DataException {
		Notification notification = storage.getObjectById(id,
				Notification.class);
		notification.setLabelIds(labelIds);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	public void updateLabelsByUser(String id, String userid,
			List<String> labelIds) throws NotFoundException, DataException {
		Notification notification = storage.getObjectByIdAndUser(id, userid,
				Notification.class);
		notification.setLabelIds(labelIds);
		notification.setUpdateTime(System.currentTimeMillis());
		storage.storeObject(notification);
	}

	public void deleteUserMessages(String userId) throws DataException {
		storage.deleteObjectsPermanently(Notification.class, userId);
	}

	/**
	 * @param userId
	 * @param syncData
	 * @return
	 * @throws DataException 
	 */
	public SyncData synchronizeByUser(String userId, SyncData input) throws DataException {
		Map<String,Object> exclude = new HashMap<String, Object>();
		exclude.put("readed", true);
		SyncData output = storage.getSyncData(input.getVersion(), userId, true, null, exclude);
		storage.cleanSyncData(input, userId, null);
		return output;
	}

	/**
	 * @param userId
	 * @param capp
	 * @param syncData
	 * @return
	 */
	public SyncData synchronizeByApp(String userId, String capp, SyncData input) throws DataException {
		Map<String,Object> exclude = new HashMap<String, Object>();
		Map<String,Object> include = new HashMap<String, Object>();
		exclude.put("readed", true);
		include.put("type", capp);
		SyncData output = storage.getSyncData(input.getVersion(), userId, true, include, exclude);
		storage.cleanSyncData(input, userId, capp);
		return output;
	}


	private static class NotificationCache {
		/**
		 * @param list
		 * @param since2
		 */
		public NotificationCache(List<Notification> list, Long since) {
			this.notifications = list; 
			this.since = since;
		}
		private List<Notification> notifications;
		private Long since;
		
	}

}
