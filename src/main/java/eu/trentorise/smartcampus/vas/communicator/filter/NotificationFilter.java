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
package eu.trentorise.smartcampus.vas.communicator.filter;

import java.io.Serializable;

public class NotificationFilter implements Serializable {
	private static final long serialVersionUID = 5704217339723155689L;

	public enum ORDERING {
		ORDER_BY_ARRIVAL, 
		ORDER_BY_REL_TIME, 
		ORDER_BY_REL_PLACE, 
		ORDER_BY_PRIORITY;
	}

	private Boolean starred = null;
	private Boolean readed = null;
	private String source;
	private String labelId;

	private String searchText;
	
	private ORDERING ordering;
	
	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public Boolean isStarred() {
		return starred;
	}

	public void setStarred(Boolean starred) {
		this.starred = starred;
	}

	public String getSourceType() {
		return source;
	}

	public void setSourceType(String source) {
		this.source = source;
	}

	public ORDERING getOrdering() {
		return ordering;
	}

	public void setOrdering(ORDERING ordering) {
		this.ordering = ordering;
	}

	public Boolean isReaded() {
		return readed;
	}

	public void setReaded(Boolean readed) {
		this.readed = readed;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
}
