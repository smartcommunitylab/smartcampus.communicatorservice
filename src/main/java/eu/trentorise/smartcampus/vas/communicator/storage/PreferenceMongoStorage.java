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
package eu.trentorise.smartcampus.vas.communicator.storage;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

public class PreferenceMongoStorage implements PreferencesStorage {

	private MongoOperations mongoTemplate = null;

	public PreferenceMongoStorage(MongoOperations mongoTemplate) {
		super();
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void store(Preference preference) throws DataException {
		mongoTemplate.save(preference);

	}

	@Override
	public void storeAll(Collection<Preference> preferences)
			throws DataException {
		try {
			for (Preference o : preferences) {
				mongoTemplate.save(o);
			}
		} catch (NullPointerException e) {
			throw new DataException();
		}

	}

	@Override
	public void delete(Preference preference) throws DataException {
		mongoTemplate.remove(preference);

	}

	@Override
	public void update(Preference preference) throws DataException {
		mongoTemplate.save(preference);

	}

	@Override
	public Preference getById(String id) throws NotFoundException,
			DataException {
		Preference result = mongoTemplate.findById(id,
				Preference.class);
		if (result == null) {
			throw new NotFoundException();
		} else {
			return result;
		}
	}

	@Override
	public List<Preference> getAll() throws DataException {
		return mongoTemplate.findAll(Preference.class);
	}

	@Override
	public Preference getByUser(String user) throws NotFoundException,
			DataException {
		if (user == null || user.isEmpty()) {
			throw new DataException();
		}

		Criteria criteria = new Criteria();
		criteria.and("user").is(user);

		Preference result = mongoTemplate.findOne(new Query(criteria),
				Preference.class);
		if (result == null) {
			throw new NotFoundException();
		} else {
			return result;
		}
	}
}
