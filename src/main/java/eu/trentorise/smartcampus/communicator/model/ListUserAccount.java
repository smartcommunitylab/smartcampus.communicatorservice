package eu.trentorise.smartcampus.communicator.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userAccounts")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListUserAccount {
	@XmlElement(name = "userAccount")
	private List<UserAccount> userAccounts;

	public List<UserAccount> getUserAccounts() {
		return userAccounts;
	}

	public void setUserAccounts(List<UserAccount> userAccounts) {
		this.userAccounts = userAccounts;
	}

}
