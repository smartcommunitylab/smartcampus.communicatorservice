package eu.trentorise.smartcampus.communicator.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "appsignature")
@XmlAccessorType(XmlAccessType.FIELD)
public class AppSignature {

	private String appId;
	
	private Map<String,String> privateKey;
	
	private Map<String,String> publicKey;


	

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Map<String,String> getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(Map<String,String> publicKey) {
		this.publicKey = publicKey;
	}

	public Map<String,String> getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(Map<String,String> privateKey) {
		this.privateKey = privateKey;
	}

}
