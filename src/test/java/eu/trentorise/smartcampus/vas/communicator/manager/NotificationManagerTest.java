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

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.trentorise.smartcampus.ac.provider.model.User;
import eu.trentorise.smartcampus.communicator.model.Notification;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.vas.communicator.filter.NotificationFilter;

public class NotificationManagerTest {

	private static NotificationManager manager;

	private static User user = null;

	@Before
	public void setup() throws DataException,NotFoundException {

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"spring/applicationContext.xml");
		manager = ctx.getBean(NotificationManager.class);
		user = new User();
		user.setId(1L);

		try {
			cleanup();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		/* Create test notifications */
		Notification n = new Notification();
		n.setId("one");
		n.setTitle("title1");
		n.setUser(Utils.userId(user));
		n.setChannelIds(Collections.singletonList("funnelFake"));
		n.setLabelIds(Collections.singletonList("label1"));
		n.setTimestamp(new GregorianCalendar(2012, 7, 30, 11, 0)
				.getTimeInMillis());
		manager.create(n);

		n = new Notification();
		n.setId("two");
		n.setTitle("title2");
		n.setUser(Utils.userId(user));
		n.setLabelIds(Arrays.asList(new String[] { "label1", "label2" }));
		n.setTimestamp(new GregorianCalendar(2012, 7, 30, 15, 0)
				.getTimeInMillis());
		manager.create(n);

		n = new Notification();
		n.setId("tree");
		n.setTitle("title3");
		n.setUser(Utils.userId(user));
		n.setLabelIds(Arrays.asList(new String[] { "label2", "label3" }));
		n.setTimestamp(new GregorianCalendar(2012, 7, 29, 11, 0)
				.getTimeInMillis());
		n.setStarred(true);
		manager.create(n);

	}

	@After
	public void cleanup() throws DataException, NotFoundException {
		manager.deleteUserMessages(user);
	}

	@Test
	public void findUnreaded() throws DataException {
		NotificationFilter filter = new NotificationFilter();
		filter.setReaded(false);
		Assert.assertEquals(3, manager.get(user, null, null, null, filter).size());
		filter.setReaded(true);
		Assert.assertEquals(0, manager.get(user, null, null, null, filter).size());
	}

	@Test
	public void find() throws DataException, NotFoundException {
		NotificationFilter filter = new NotificationFilter();

		Assert.assertEquals(3, manager.get(user, null, null, null, null).size());
		Assert.assertEquals(1, manager.get(user, null, 2, 3, null).size());
		Assert.assertEquals(2, manager.get(user, null, 1, 3, null).size());

//		filter.setFunnelId("funnelFake");
		Assert.assertEquals(
				1,
				manager.get(user, null, null, null, filter).size());

//		filter.setFunnelId("funnelFake1");
		Assert.assertEquals(
				0,
				manager.get(user, null, null, null, filter).size());

		filter = new NotificationFilter();
		filter.setLabelId("label3");
		Assert.assertEquals(
				1,
				manager.get(user, null, null, null, filter).size());

		filter.setLabelId("label1");
		Assert.assertEquals(
				2,
				manager.get(user, null, null, null, filter).size());

		filter = new NotificationFilter();
		filter.setStarred(true);
		Assert.assertEquals(
				1,
				manager.get(
						user,
						new GregorianCalendar(2012, 7, 25, 11, 0)
								.getTimeInMillis(), null, null, filter).size());

		Assert.assertEquals(
				0,
				manager.get(
						user,
						new GregorianCalendar(2012, 7, 31, 11, 0)
								.getTimeInMillis(), null, null, filter).size());

		Assert.assertEquals(
				3,
				manager.get(
						user,
						new GregorianCalendar(2012, 7, 25, 11, 0)
								.getTimeInMillis(), null, null, null).size());

		Assert.assertEquals(
				0,
				manager.get(
						user,
						new GregorianCalendar(2012, 7, 31, 11, 0)
								.getTimeInMillis(), null, null, null).size());

		Assert.assertEquals(
				1,
				manager.get(
						user,
						new GregorianCalendar(2012, 7, 30, 14, 0)
								.getTimeInMillis(), null, null, null).size());

		Assert.assertNotNull(manager.getById("one"));

	}

	@Test
	public void edit() throws DataException, NotFoundException {

		NotificationFilter  filter = new NotificationFilter();
		filter.setStarred(true);
		Assert.assertEquals(1, manager.get(user, null, null, null, filter)
				.size());

		manager.starred("one");

		Assert.assertEquals(2, manager.get(user, null, null, null, filter)
				.size());

		filter.setStarred(null);
		filter.setLabelId("label3");
		Assert.assertEquals(
				1,
				manager.get(user, null, null, null, filter).size());

		manager.updateLabels("one", Arrays.asList(new String[] { "label3" }));

		Assert.assertEquals(
				2,
				manager.get(user, null, null, null, filter).size());
	}
}
