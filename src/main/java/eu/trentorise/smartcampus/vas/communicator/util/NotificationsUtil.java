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
package eu.trentorise.smartcampus.vas.communicator.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.communicator.model.Action;
import eu.trentorise.smartcampus.communicator.model.Channel;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;

public class NotificationsUtil {

	private static final int ACTION_FOLDER = 0;
	private static final int ACTION_MAIL = 1;
	private static final String FOLDER_STARRED = "Starred";
	private static final String FOLDER_TRASH = "Trash";
	private static final String SEPARATOR = "------------------------------------------------------------------------------------------";

	public static boolean applyChannelActions(Channel channel, Notification notification, Set<String> toSend) {
		if (channel.getActions() != null) {
			for (Action action : channel.getActions()) {
				switch (action.getType()) {
				case ACTION_FOLDER:
					if (FOLDER_TRASH.equals(action.getValue())) {
						return false;
					} else if (FOLDER_STARRED.equals(action.getValue())) {
						notification.setStarred(true);
					}
					break;
				case ACTION_MAIL:
					toSend.add(action.getValue());
					break;
				}
			}
		}
		return true;
	}

	public static  void sendEmail(Map<String, List<Notification>> toSend, MailNotificationSender sender) throws DataException {
			for (String to : toSend.keySet()) {
				StringBuffer body = new StringBuffer();
				for (Notification notification : toSend.get(to)) {
					body.append(notification.getTitle() + "\n" + notification.getDescription() + "\n" + SEPARATOR + "\n");
				}
				String subject = "New notification" + ((toSend.get(to).size() > 1) ? "s" : "");
				subject += " from SmartCampus Communicator";
				sender.sendNotification(subject, body.toString(), "communicator@smartcampuslab.it", to);
			}
	}

}
