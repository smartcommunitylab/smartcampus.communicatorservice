package eu.trentorise.smartcampus.vas.communicator.manager.pushservice.impl;

import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.exceptions.NotFoundException;
import eu.trentorise.smartcampus.vas.communicator.manager.pushservice.PushServiceCloud;



@Component
public class ApplePushNotificationServiceManager implements PushServiceCloud {

	@Override
	public boolean sendToCloud(Notification notification)
			throws NotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

}
