package eu.trentorise.smartcampus.communicator.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "appAccounts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListAppAccount {

	@XmlElement(name = "appAccount")
	private List<AppAccount> appAccounts;

	public List<AppAccount> getAppAccounts() {
		return appAccounts;
	}

	public void setAppAccounts(List<AppAccount> appAccounts) {
		this.appAccounts = appAccounts;
	}

}
