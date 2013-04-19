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
package eu.trentorise.smartcampus.vas.communicator.manager;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.LabelObject;
import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;

public class PreferenceTest {

	private static CommunicatorManager manager;

	private static User user = null;

	@BeforeClass
	public static void setup() throws DataException {

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"spring/applicationContext.xml");
		manager = ctx.getBean(CommunicatorManager.class);
		user = new User();
		user.setId(1L);

		Preference p = manager.getPreferences(user);
		p.setLabels(new LabelObject[] { new LabelObject("1", "work", "000000"), new LabelObject("2", "hobbies", "FFFFFF") });
		manager.storePreferences(user, p);
	}

	@AfterClass
	public static void cleanup() throws DataException {
		manager.deleteUser(user);
	}

	@Test
	public void find() throws DataException {
		Assert.assertNotNull(manager.getPreferences(user));
	}

}
