package eu.trentorise.smartcampus.communicatorservice.manager;

import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import eu.trentorise.smartcampus.communicator.model.AppAccount;
import eu.trentorise.smartcampus.communicatorservice.exceptions.AlreadyExistException;
import eu.trentorise.smartcampus.presentation.common.exception.NotFoundException;

@Service
public class AppAccountManager {
	private static final Logger logger = Logger
			.getLogger(AppAccountManager.class);

	private static final int FIRST = 0;

	@Autowired
	MongoTemplate db;

	public AppAccount save(AppAccount appAccount) throws AlreadyExistException {

		if (appAccount.getAppId() != null
				&& db.findById(appAccount.getAppId(), AppAccount.class) != null) {
			logger.error("AppAccount already stored, "
					+ appAccount.getAppId());
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

	public List<AppAccount> getAppAccounts(String appId) {
		Criteria crit = new Criteria();
		crit.and("appId").is(appId);
		Query query = Query.query(crit);
		return db.find(query, AppAccount.class);
	}

	public AppAccount getAppAccount(String appId) {
		Criteria crit = new Criteria();
		crit.and("appId").is(appId);
		Query query = Query.query(crit);
		List<AppAccount> x = db.find(query, AppAccount.class);
		if (x.isEmpty())
			return null;

		return x.get(FIRST);
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
