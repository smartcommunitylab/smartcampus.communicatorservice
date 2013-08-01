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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import eu.trentorise.smartcampus.communicator.model.CloudToPushType;
import eu.trentorise.smartcampus.social.model.User;

public class Utils {

	@Autowired
	@Value("${gcm.sender.id}")
	public static String gcm_sender_id;

	public static String userId(User user) {
		return user.getId() + "";
	}

	public static CloudToPushType checkCloudToPushType(String conf) {
		String result = conf.toUpperCase();
		for (CloudToPushType x : CloudToPushType.values()) {
			if (x.name().compareTo(result) == 0) {
				return x;
			}
		}
		return CloudToPushType.GOOGLE;
	}

}
