package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;

import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.PushException;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

@Component
public class ApplePushNotificationServiceManager implements PushServiceCloud {

	@Override
	public void sendToCloud(Notification notification)
			throws NotFoundException, NoUserAccount, PushException {
		// TODO Auto-generated method stub
	}

}
