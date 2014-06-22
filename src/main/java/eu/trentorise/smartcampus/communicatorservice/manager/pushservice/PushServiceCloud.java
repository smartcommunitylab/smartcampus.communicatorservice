package eu.trentorise.smartcampus.communicatorservice.manager.pushservice;

import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.communicatorservice.exceptions.NoUserAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.PushException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

public interface PushServiceCloud {

	public abstract void sendToCloud(Notification notification)
			throws NotFoundException, NoUserAccount, PushException;

}