
package acme.features.authenticated.bulletin;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Bulletin;
import acme.framework.components.accounts.Authenticated;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;

@Service
public class AuthenticatedBulletinListService extends AbstractService<Authenticated, Bulletin> {

	@Autowired
	protected AuthenticatedBulletinRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		super.getResponse().setChecked(true);
	}

	@Override
	public void authorise() {
		boolean status;
		status = super.getRequest().getPrincipal().isAuthenticated();
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<Bulletin> objects;
		Date date;

		date = new Date();
		objects = this.repository.findAllBulletins(date);
		super.getBuffer().setData(objects);
	}

	@Override
	public void unbind(final Bulletin object) {
		assert object != null;
		Tuple tuple;
		String payload;

		tuple = super.unbind(object, "title", "instantiationMoment");
		payload = String.format("%s;%s;%s", object.getMessage(), object.getLink(), object.isCritical());
		tuple.put("payload", payload);

		super.getResponse().setData(tuple);
	}

}
