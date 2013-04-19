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

import eu.trentorise.smartcampus.communicator.model.Preference;
import eu.trentorise.smartcampus.presentation.common.exception.DataException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

public interface PreferencesStorage {

	public void store(Preference preference) throws DataException;

	public void storeAll(Collection<Preference> preferences)
			throws DataException;

	public void delete(Preference preference) throws DataException;

	public void update(Preference preference) throws DataException;

	public Preference getById(String id) throws NotFoundException,
			DataException;

	public Preference getByUser(String user) throws NotFoundException,
			DataException;

	public List<Preference> getAll() throws DataException;

}
