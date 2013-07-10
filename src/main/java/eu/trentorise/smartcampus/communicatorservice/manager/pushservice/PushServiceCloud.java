package eu.trentorise.smartcampus.communicatorservice.manager.pushservice;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;




public interface PushServiceCloud {

	public abstract boolean sendToCloud(Notification notification)
			throws NotFoundException;

}