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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;
import eu.trentorise.smartcampus.presentation.storage.BasicObjectStorage;

@Component
public class PreferenceManager {

	@Autowired
	BasicObjectStorage storage;

	public List<Preference> get() throws DataException {
		return storage.getObjectsByType(Preference.class);
	}

	public Preference getById(String id) throws NotFoundException, DataException {
		return storage.getObjectById(id, Preference.class);
	}

	public Preference getByUser(String user) throws NotFoundException, DataException {
		List<Preference> list = storage.getObjectsByType(Preference.class,user);
		if (list == null || list.size() == 0) {
			throw new NotFoundException();
		} else if (list.size() > 1) {
			throw new DataException("More than one preference object for user");
		}
		return list.get(0);
	}

	public void create(Preference preference) throws DataException {
		storage.storeObject(preference);
	}

	public void delete(Preference preference) throws DataException {
		storage.deleteObject(preference);
	}

	public void update(Preference preference) throws DataException {
		storage.storeObject(preference);
	}
}
