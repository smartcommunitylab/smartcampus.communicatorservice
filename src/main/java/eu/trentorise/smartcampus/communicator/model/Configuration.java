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

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * <i>Configuration</i> is the representation of a configuration in a user
 * storage account
 * 
 * @author mirko perillo
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

	private static ObjectMapper mapper = new ObjectMapper();
	/**
	 * value of configuration
	 */
	private CloudToPushType key;

	private String listValue;

	public Configuration() {

	}
	
	public Configuration(CloudToPushType key, Map<String, String> listValue) throws JsonGenerationException, JsonMappingException, IOException {
		this.setKey(key);
		this.setListValue(listValue);
	}

	public CloudToPushType getKey() {
		return key;
	}

	public void setKey(CloudToPushType key) {
		this.key = key;
	}
	
	public void setListValue(Map<String, String> listValue) throws JsonGenerationException, JsonMappingException, IOException {
		this.listValue = mapper.writeValueAsString(listValue);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getListValue() {
		try {
			return mapper.readValue(listValue, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

}
