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
package eu.trentorise.smartcampus.communicator.model;

import java.util.List;

import eu.trentorise.smartcampus.presentation.data.BasicObject;


public class Preference extends BasicObject {
	private static final long serialVersionUID = -572246797611393859L;

	public static final int DEF_MAX_MESSAGES = 1000;
	public static final boolean DEF_SYNC_AUTO = true;
	public static final int DEF_SYNC_PERIOD = 5;

	private Integer maxMessageNumber;
	private LabelObject[] labels;
	private List<Action> actions;
	private boolean synchronizeAutomatically = DEF_SYNC_AUTO;
	private Integer syncPeriod = DEF_SYNC_PERIOD;


	public LabelObject[] getLabels() {
		return labels;
	}

	public void setLabels(LabelObject[] labels) {
		this.labels = labels;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public Integer getMaxMessageNumber() {
		return maxMessageNumber;
	}

	public void setMaxMessageNumber(Integer maxMessageNumber) {
		this.maxMessageNumber = maxMessageNumber;
	}

	public boolean isSynchronizeAutomatically() {
		return synchronizeAutomatically;
	}

	public void setSynchronizeAutomatically(boolean synchronizeAutomatically) {
		this.synchronizeAutomatically = synchronizeAutomatically;
	}

	public Integer getSyncPeriod() {
		return syncPeriod;
	}

	public void setSyncPeriod(Integer syncPeriod) {
		this.syncPeriod = syncPeriod;
	}
}
