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
package eu.trentorise.smartcampus.profileservice.managers;

import it.unitn.disi.sweb.webapi.client.WebApiException;
import it.unitn.disi.sweb.webapi.model.smartcampus.social.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trentorise.smartcampus.ac.provider.model.Attribute;
import eu.trentorise.smartcampus.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.exceptions.SmartCampusException;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.profileservice.managers.CommunityManagerException;
import eu.trentorise.smartcampus.profileservice.managers.ProfileManager;
import eu.trentorise.smartcampus.profileservice.model.ExtendedProfile;
import eu.trentorise.smartcampus.profileservice.storage.ProfileStorage;
import eu.trentorise.smartcampus.test.SocialEngineOperation;

/**
 * Test Class
 * 
 * @author mirko periillo
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/applicationContext.xml")
public class ProfileManagerTest {

	@Autowired
	ProfileManager profileManager;

	@Autowired
	ProfileStorage storage;

	@Autowired
	SocialEngineOperation socialOperation;

	@Test
	public void crudExtendedProfile() throws CommunityManagerException,
			AlreadyExistException, SmartCampusException, WebApiException,
			NotFoundException {
		User socialUser = socialOperation.createUser();

		eu.trentorise.smartcampus.ac.provider.model.User user = new eu.trentorise.smartcampus.ac.provider.model.User();
		user.setId(1l);
		user.setSocialId(socialUser.getId());
		ExtendedProfile p = new ExtendedProfile();
		p.setAppId("appId");
		p.setProfileId("profileId");
		p.setUserId("1");
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("receiveUpdates", true);
		p.setContent(content);
		p = profileManager.create(user, p);
		socialOperation.deleteUser(socialUser.getId());
		Assert.assertNotNull(p.getId());
		Assert.assertNotNull(p.getSocialId());
		Assert.assertTrue(profileManager.deleteExtendedProfile(p.getId()));
	}

	@Test
	public void profileByUserIds() throws CommunityManagerException {
		eu.trentorise.smartcampus.ac.provider.model.User u = new eu.trentorise.smartcampus.ac.provider.model.User();
		u.setId(10l);
		Attribute name = new Attribute();
		name.setKey("eu.trentorise.smartcampus.givenname");
		name.setValue("user1");
		Attribute surname = new Attribute();
		surname.setKey("eu.trentorise.smartcampus.surname");
		surname.setValue("surname");

		u.setAttributes(Arrays.asList(name, surname));
		profileManager.getOrCreateProfile(u);

		u = new eu.trentorise.smartcampus.ac.provider.model.User();
		u.setId(15l);
		name = new Attribute();
		name.setKey("eu.trentorise.smartcampus.givenname");
		name.setValue("user2");
		surname = new Attribute();
		surname.setKey("eu.trentorise.smartcampus.surname");
		surname.setValue("surname");

		u.setAttributes(Arrays.asList(name, surname));
		profileManager.getOrCreateProfile(u);

		u = new eu.trentorise.smartcampus.ac.provider.model.User();
		u.setId(101010l);
		name = new Attribute();
		name.setKey("eu.trentorise.smartcampus.givenname");
		name.setValue("user3");
		surname = new Attribute();
		surname.setKey("eu.trentorise.smartcampus.surname");
		surname.setValue("surname");

		u.setAttributes(Arrays.asList(name, surname));
		profileManager.getOrCreateProfile(u);

		profileManager.getUsers();
		profileManager.getUsers(Arrays.asList("110", "1101010", "200"));
	}

	@Test
	public void extProfileByAttrs() throws CommunityManagerException,
			AlreadyExistException, SmartCampusException, WebApiException,
			DataException {

		// cleaning
		for (ExtendedProfile extP : storage.findExtendedProfiles("10", "appId")) {
			storage.deleteExtendedProfile(extP.getId());
		}

		for (ExtendedProfile extP : storage.findExtendedProfiles("15", "appId")) {
			storage.deleteExtendedProfile(extP.getId());
		}
		User socialUser = socialOperation.createUser();

		// user1
		eu.trentorise.smartcampus.ac.provider.model.User u = new eu.trentorise.smartcampus.ac.provider.model.User();
		u.setId(10l);
		u.setSocialId(socialUser.getId());
		Attribute name = new Attribute();
		name.setKey("eu.trentorise.smartcampus.givenname");
		name.setValue("user1");
		Attribute surname = new Attribute();
		surname.setKey("eu.trentorise.smartcampus.surname");
		surname.setValue("surname");
		u.setAttributes(Arrays.asList(name, surname));

		// profile user 1
		ExtendedProfile profile = new ExtendedProfile();
		profile.setAppId("appId");
		profile.setUserId("10");
		profile.setProfileId("preferences");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("pref1", "value");
		profile.setContent(m);
		profileManager.create(u, profile);

		// user2
		u = new eu.trentorise.smartcampus.ac.provider.model.User();
		u.setId(15l);
		u.setSocialId(socialUser.getId());
		name = new Attribute();
		name.setKey("eu.trentorise.smartcampus.givenname");
		name.setValue("user2");

		surname = new Attribute();
		surname.setKey("eu.trentorise.smartcampus.surname");
		surname.setValue("surname");
		u.setAttributes(Arrays.asList(name, surname));

		// profile user2
		profile = new ExtendedProfile();
		profile.setAppId("appId");
		profile.setProfileId("preferences");
		profile.setUserId("15");
		m = new HashMap<String, Object>();
		m.put("pref1", "value");
		profile.setContent(m);
		profileManager.create(u, profile);

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("pref1", "value");
		storage.findExtendedProfiles("appId", "preferences", filter);

		socialOperation.deleteUser(socialUser.getId());

	}
}
