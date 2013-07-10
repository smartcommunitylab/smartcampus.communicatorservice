/**
 *    Copyright 2012-2013 Trento RISE
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
 */

package eu.trentorise.smartcampus.communicator.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User storage account informations
 * 
 * @author mirko perillo
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAccount {
	/**
	 * id of the account
	 */
	private String id;
	/**
	 * id of the user
	 */
	private String userId;

	private String appId;

	/**
	 * list of the configurations of the account storage
	 */
	@XmlElementWrapper
	@XmlElement(name = "configuration")
	private List<Configuration> configurations;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	public List<CloudToPushType> getCloudToPushTypeConfigured() {
		List<Configuration> list = getConfigurations();
		List<CloudToPushType> result = new ArrayList<CloudToPushType>();
		for (Configuration c : list) {
			if (c.getKey() != null)
				result.add(c.getKey());
		}
		return result;
	}

	public Configuration getSpecificConfiguration(CloudToPushType conf) {
		List<Configuration> list = getConfigurations();
		Configuration result = null;
		for (Configuration c : list) {
			if (conf.compareTo(c.getKey()) == 0)
				result = c;
		}
		return result;
	}
	

}
