package eu.trentorise.smartcampus.communicatorservice.manager;

import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


import eu.trentorise.smartcampus.exceptions.*;
import eu.trentorise.smartcampus.communicator.model.AppAccount;


@Service
public class AppAccountManager {
	private static final Logger logger = Logger
			.getLogger(AppAccountManager.class);

	@Autowired
	MongoTemplate db;

	public AppAccount save(AppAccount appAccount) throws AlreadyExistException {

		if (appAccount.getAppName() != null
				&& db.findById(appAccount.getAppName(), AppAccount.class) != null) {
			logger.error("AppAccount already stored, " + appAccount.getAppName());
			throw new AlreadyExistException();
		}
		if (appAccount.getId() == null
				|| appAccount.getId().trim().length() == 0) {
			appAccount.setId(new ObjectId().toString());
		}

		db.save(appAccount);
		return appAccount;
	}

	public AppAccount update(AppAccount appAccount) throws NotFoundException {
		AppAccount toUpdate = getAppAccountById(appAccount.getId());
		toUpdate = update(toUpdate, appAccount);
		db.save(toUpdate);
		return toUpdate;
	}

	private AppAccount update(AppAccount destination, AppAccount source) {
		destination.setConfigurations(source.getConfigurations());
		return destination;
	}

	public void delete(String appAccountId) {
		Criteria crit = new Criteria();
		crit.and("id").is(appAccountId);
		Query query = Query.query(crit);
		db.remove(query, AppAccount.class);
	}

	public List<AppAccount> getAppAccounts(String appName) {
		Criteria crit = new Criteria();
		crit.and("appName").is(appName);
		Query query = Query.query(crit);
		return db.find(query, AppAccount.class);
	}
	
	public AppAccount getAppAccount(String appName) {
		Criteria crit = new Criteria();
		crit.and("appName").is(appName);
		Query query = Query.query(crit);
		return db.find(query, AppAccount.class).get(0);//todo
	}

	public AppAccount getAppAccountById(String appAccountId)
			throws NotFoundException {
		AppAccount appAccount = db.findById(appAccountId, AppAccount.class);
		if (appAccount == null) {
			throw new NotFoundException();
		}
		return appAccount;
	}

}
