package eu.trentorise.smartcampus.communicatorservice.manager.pushservice.impl;

import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicatorservice.manager.pushservice.PushServiceCloud;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

@Component
public class ApplePushNotificationServiceManager implements PushServiceCloud {

	@Override
	public boolean sendToCloud(Notification notification)
			throws NotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

}
