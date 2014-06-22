package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.android.gcm.server.Message;
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

@Component
public class GoogleCloudMessengerManager implements PushServiceCloud {

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
	public void sendToCloud(Notification notification)
			throws NotFoundException, NoUserAccount, PushException {

		// in default case is the system messenger that send
		String senderId = null;
		Sender sender = null;
		String senderAppName = notification.getAuthor().getAppId();

		AppAccount appAccount;
		List<AppAccount> listApp = appAccountManager
				.getAppAccounts(senderAppName);

		appAccount = listApp.get(0);

		List<Configuration> listConfApp = appAccount.getConfigurations();
		Iterator<Configuration> indexConf = listConfApp.iterator();

		while (indexConf.hasNext() && senderId == null) {
			Configuration conf = indexConf.next();
			if (CloudToPushType.GOOGLE.compareTo(conf.getKey()) == 0 && conf.get(gcm_sender_key)!=null) {
				senderId = conf.get(gcm_sender_key) ;
			} else {
				throw new NotFoundException();
			}
		}

		sender = new Sender(senderId);

		String registrationId = "";

		List<UserAccount> listUserAccount = userAccountManager
				.findByUserIdAndAppName(notification.getUser(), senderAppName);

		if (!listUserAccount.isEmpty() && sender!=null) {

			UserAccount userAccountSelected = listUserAccount.get(0);

			List<Configuration> listConfUser = userAccountSelected
					.getConfigurations();
			if(listConfUser!=null && !listConfUser.isEmpty()){
				for (Configuration index : listConfUser) {
					if (CloudToPushType.GOOGLE.compareTo(index.getKey()) == 0) {
						registrationId = index.get(
								gcm_registration_id_key);
						break;
					}
				}
				Message message = new Message.Builder()
				.collapseKey("1")
				.timeToLive(3)
				.delayWhileIdle(true)
				.addData(notification.getTitle(),
						notification.getDescription()).build();
				try {
					sender.send(message, registrationId, 5);
				} catch (Exception e) {
					e.printStackTrace();
					throw new PushException(e);
				}
			}
			
		}else{
			throw new NoUserAccount("The user is not register for receive push notification");
		}
	}
}
