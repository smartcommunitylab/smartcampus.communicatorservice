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

	private String privateKey;

	private String publicKey;

	public Configuration() {

	}

	public Configuration(CloudToPushType key, Map<String, String> privateKey,
			Map<String, String> publicKey) throws JsonGenerationException,
			JsonMappingException, IOException {
		this.setKey(key);
		this.setPublicKey(publicKey);
		this.setPrivateKey(privateKey);
	}

	public Configuration(CloudToPushType key, Map<String, String> privateKey)
			throws JsonGenerationException, JsonMappingException, IOException {
		this.setKey(key);
		this.setPrivateKey(privateKey);
	}

	public CloudToPushType getKey() {
		return key;
	}

	public void setKey(CloudToPushType key) {
		this.key = key;
	}

	public void setPublicKey(Map<String, String> listValue)
			throws JsonGenerationException, JsonMappingException, IOException {
		this.publicKey = mapper.writeValueAsString(listValue);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getPublicKey() {
		try {
			return mapper.readValue(publicKey, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	public void setPrivateKey(Map<String, String> listValue)
			throws JsonGenerationException, JsonMappingException, IOException {
		this.privateKey = mapper.writeValueAsString(listValue);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getPrivateKey() {
		try {
			return mapper.readValue(privateKey, Map.class);
		} catch (Exception e) {
			return null;
		}
	}

	public String get(String key) {
		if (getPrivateKey().get(key) != null)
			return getPrivateKey().get(key);
		else
			return getPublicKey().get(key);
	}

	public void remove(String key) {
		getPrivateKey().remove(key);
		getPublicKey().remove(key);
	}

	public void putPublic(String key, String value) {
		getPublicKey().put(key, value);

	}

	public void putPrivate(String key, String value) {
		getPrivateKey().put(key, value);

	}

}
