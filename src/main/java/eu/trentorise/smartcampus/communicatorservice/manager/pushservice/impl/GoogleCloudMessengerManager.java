package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.common.util.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Priority;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.communicator.model.Configuration;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.UserAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.PushException;
import eu.trentorise.smartcampus.communicatorservice.manager.AppAccountManager;
import eu.trentorise.smartcampus.communicatorservice.manager.UserAccountManager;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

@SuppressWarnings("deprecation")
@Component
public class GoogleCloudMessengerManager implements PushServiceCloud {

	Logger logger = LoggerFactory.getLogger(getClass());

	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(Feature.WRITE_NULL_PROPERTIES, false);
	}

	@Autowired
	AppAccountManager appAccountManager;

	@Autowired
	UserAccountManager userAccountManager;

	@Autowired
	@Value("${gcm.sender.key}")
	private String gcm_sender_key;

	@Autowired
	@Value("${gcm.registration.id.key}")
	private String gcm_registration_id_key;

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.trentorise.smartcampus.vas.communicator.manager.pushservice.
	 * PushServiceCloud
	 * #sendToCloud(eu.trentorise.smartcampus.communicator.model.Notification)
	 */
	@Override
	public void sendToCloud(Notification notification) throws NotFoundException, NoUserAccount, PushException {
		if (notification.getUser() != null) {
			sendToCloudUser(notification);
		} else if (notification.getChannelIds() != null && !notification.getChannelIds().isEmpty()) {
			sendToCloudTopics(notification);
		}
	}
	
	private void sendToCloudUser(Notification notification) throws NotFoundException, NoUserAccount, PushException {

		
		// in default case is the system messenger that send
		String senderId = null;
		FCMSender sender = null;
		String senderAppName = notification.getAuthor().getAppId();

		AppAccount appAccount;
		List<AppAccount> listApp = appAccountManager.getAppAccounts(senderAppName);

		appAccount = listApp.get(0);

		List<Configuration> listConfApp = appAccount.getConfigurations();
		Iterator<Configuration> indexConf = listConfApp.iterator();

		while (indexConf.hasNext() && senderId == null) {
			Configuration conf = indexConf.next();
			if (CloudToPushType.GOOGLE.compareTo(conf.getKey()) == 0 && conf.get(gcm_sender_key) != null) {
				senderId = conf.get(gcm_sender_key);
			} else {
				throw new NotFoundException();
			}
		}

		sender = new FCMSender(senderId);

		String registrationId = "";

		logger.info("Sending message to user: "+senderAppName+" -> "+notification.getUser());

		UserAccount listUserAccount = userAccountManager.findByUserIdAndAppName(notification.getUser(), senderAppName);

		if (listUserAccount != null && sender != null) {

			UserAccount userAccountSelected = listUserAccount;

			List<Configuration> listConfUser = userAccountSelected.getConfigurations();
			if (listConfUser != null && !listConfUser.isEmpty()) {
				Message.Builder message = new Message.Builder().collapseKey("").delayWhileIdle(true).addData("title", notification.getTitle()).addData("description", notification.getDescription());
				if (notification.getContent() != null) {
					for (String key : notification.getContent().keySet()) {
						if (key.startsWith("_")) {
							continue;
						}						
						if (notification.getContent().get(key) != null) {
							message.addData("content." + key, notification.getContent().get(key).toString());
						}
					}
				}
				if (notification.getEntities() != null && !notification.getEntities().isEmpty()) {
					try {
						message.addData("entities", mapper.writeValueAsString(notification.getEntities()));
					} catch (Exception e) {
						logger.warn("Failed to convert entities: " + e.getMessage());
					}
				}
				
				message.addData("content-available", "1");
				message.addData("body", notification.getDescription());
				message.addData("title", notification.getTitle());
				
				message.priority(Priority.HIGH);
				
				List<String> regIds = new ArrayList<String>();
				List<String> iosRegIds = new ArrayList<String>();
				
				for (Configuration index : listConfUser) {
					
					if (CloudToPushType.GOOGLE.compareTo(index.getKey()) == 0) {
						registrationId = index.get(gcm_registration_id_key);
						if (registrationId != null) {
							String platform = index.getPrivateKey().get("platform");
							if ("android".equalsIgnoreCase(platform)) {
								regIds.add(registrationId);
							} else {
								iosRegIds.add(registrationId);
							}
						}
					}
				}


				try {
					if (regIds.size() > 0) {
						logger.info("Sending android push to "+regIds);
						com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
						builder.title(notification.getTitle()).body(notification.getDescription());
						message.notification(builder.build());
						Message n = message.build();
						MulticastResult result = sender.send(n, regIds, 1);
						cleanRegistrations(userAccountSelected, regIds, result.getResults());
						logger.info("Android push result "+result);
					}
					if (iosRegIds.size() > 0) {
						com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
						builder.title(notification.getTitle()).body(notification.getDescription());
						message.notification(builder.build());

						logger.info("Sending iOS push to "+iosRegIds);
						MulticastResult result = sender.send(message.build(), iosRegIds, 1);
						cleanRegistrations(userAccountSelected, iosRegIds, result.getResults());
						logger.info("iOS push result "+result);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					throw new PushException(e);
				}
			}

		} else {
			throw new NoUserAccount("The user "+notification.getUser()+" is not register for receive push notification");
		}
	}

	/**
	 * @param userAccountSelected 
	 * @param regIds
	 * @param results
	 * @throws AlreadyExistException 
	 */
	private void cleanRegistrations(UserAccount account, List<String> regIds, List<Result> results) throws AlreadyExistException {
		Set<String> toRemove = new HashSet<String>();
		for (int i = 0; i < results.size(); i++) {
			Result res = results.get(i);
			if (StringUtils.isEmpty(res.getMessageId())) {
				toRemove.add(regIds.get(i));
			}
		}
		List<Configuration> configs = new LinkedList<Configuration>();
		for (Configuration c : account.getConfigurations()) {
			if (c.get(gcm_registration_id_key) != null && !toRemove.contains(c.get(gcm_registration_id_key))) {
				configs.add(c);
			}
		}
		logger.info("cleaning user ("+account.getUserId()+") configs: "+toRemove);
		account.setConfigurations(configs);
		userAccountManager.update(account);
	}

	private void sendToCloudTopics(Notification notification) throws PushException, NotFoundException {
		String senderId = null;
		FCMSender sender = null;
		String senderAppName = notification.getAuthor().getAppId();

		AppAccount appAccount;
		List<AppAccount> listApp = appAccountManager.getAppAccounts(senderAppName);

		appAccount = listApp.get(0);

		List<Configuration> listConfApp = appAccount.getConfigurations();
		Iterator<Configuration> indexConf = listConfApp.iterator();

		while (indexConf.hasNext() && senderId == null) {
			Configuration conf = indexConf.next();
			if (CloudToPushType.GOOGLE.compareTo(conf.getKey()) == 0 && conf.get(gcm_sender_key) != null) {
				senderId = conf.get(gcm_sender_key);
			} else {
				throw new NotFoundException();
			}
		}

		sender = new FCMSender(senderId);

		for (String topic : notification.getChannelIds()) {

			Message.Builder message = new Message.Builder().collapseKey("").delayWhileIdle(true).addData("title", notification.getTitle()).addData("description", notification.getDescription());
			if (notification.getContent() != null) {
				for (String key : notification.getContent().keySet()) {
					if (key.startsWith("_")) {
						continue;
					}
					if (notification.getContent().get(key) != null) {
						message.addData("content." + key, notification.getContent().get(key).toString());
					}
				}
			}
			if (notification.getEntities() != null && !notification.getEntities().isEmpty()) {
				try {
					message.addData("entities", mapper.writeValueAsString(notification.getEntities()));
				} catch (Exception e) {
					logger.warn("Failed to convert entities: " + e.getMessage());
				}
			}
			
			message.addData("content-available", "1");
			message.addData("body", notification.getDescription());
			message.addData("title", notification.getTitle());
			message.priority(Priority.HIGH);

			// REQUIRED ON IOS TO WORK
			com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
			builder.title(notification.getTitle()).body(notification.getDescription());
			message.notification(builder.build());
			
			try {
				logger.info("SENDING ANDROID " + message);
				Result sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + topic +".android", 1);
				logger.info("SENDING ANDROID RESULT " + sendresult);
				
				// REQUIRED ON IOS TO WORK
				logger.info("SENDING IOS " + message);
				sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + topic+".ios", 1);
				logger.info("SENDING IOS RESULT " + sendresult);
				logger.info("SENDING LEGACY " + message);
				sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + topic, 1);
				logger.info("SENDING LEGACY RESULT " + sendresult);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new PushException(e);
			}
		}
	}

	public static void main(String[] args) throws PushException {
		FCMSender sender = new FCMSender("AAAAiij7hrg:APA91bFJF-_5mW-oMHK87ntYqfki5627tumHf4Opim0raPlj_DzdL6o9XLuHb-xcViKghr8mJIkVQszH2RdhSYB9KAL9M2oz1vtJqwe53teAKdVc2AGVdi-qKyi9Ep2l_cQOxItEFq9r");

		Message.Builder message = new Message.Builder().collapseKey("").delayWhileIdle(true).addData("title", "tit").addData("description", "descr");
		Map<String, Object> content = new HashMap<String, Object>();
		if (content != null) {
			for (String key :  content.keySet()) {
				if (key.startsWith("_")) {
					continue;
				}
				if (content.get(key) != null) {
					message.addData("content." + key, content.get(key).toString());
				}
			}
		}
		message.addData("content-available", "1");
		message.addData("body", "descr");
		message.addData("title", "tit");
		message.priority(Priority.HIGH);

		// REQUIRED ON IOS TO WORK
		com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
		builder.title("tit").body("descr");
		message.notification(builder.build());
		
		try {
			Result sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + "ferraraplaygo" +".android", 1);
			
			// REQUIRED ON IOS TO WORK
			sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + "ferraraplaygo"+".ios", 1);
			sendresult = sender.send(message.build(), Constants.TOPIC_PREFIX + "ferraraplaygo", 1);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new PushException(e);
		}

	}
	
}
