package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.google.android.gcm.server.Sender;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.communicator.model.Configuration;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicator.model.UserAccount;
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
		Sender sender = null;
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

		sender = new Sender(senderId);

		String registrationId = "";

		List<UserAccount> listUserAccount = userAccountManager.findByUserIdAndAppName(notification.getUser(), senderAppName);

		if (!listUserAccount.isEmpty() && sender != null) {

			UserAccount userAccountSelected = listUserAccount.get(0);

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
				
				message.priority(Priority.HIGH);
				com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
				builder.title(notification.getTitle()).body(notification.getDescription());
				message.notification(builder.build());
				

				List<String> regIds = new ArrayList<String>();
				for (Configuration index : listConfUser) {
					if (CloudToPushType.GOOGLE.compareTo(index.getKey()) == 0) {
						registrationId = index.get(gcm_registration_id_key);
						if (registrationId != null) {
							regIds.add(registrationId);
						}
					}
				}


				try {
					sender.send(message.build(), regIds, 1);
				} catch (Exception e) {
					e.printStackTrace();
					throw new PushException(e);
				}
			}

		} else {
			throw new NoUserAccount("The user "+notification.getUser()+" is not register for receive push notification");
		}
	}

	private void sendToCloudTopics(Notification notification) throws PushException, NotFoundException {
		String senderId = null;
		Sender sender = null;
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

		sender = new Sender(senderId);

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
			
			message.priority(Priority.HIGH);
			com.google.android.gcm.server.Notification.Builder builder = new com.google.android.gcm.server.Notification.Builder("");
			builder.title(notification.getTitle()).body(notification.getDescription());
			message.notification(builder.build());
			
			try {
				sender.send(message.build(), Constants.TOPIC_PREFIX + topic, 1);
			} catch (Exception e) {
				e.printStackTrace();
				throw new PushException(e);
			}
		}
	}

}
