
package acme.features.authenticated.lecturer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.framework.components.accounts.Authenticated;
import acme.framework.components.accounts.Principal;
import acme.framework.components.accounts.UserAccount;
import acme.framework.components.models.Tuple;
import acme.framework.controllers.HttpMethod;
import acme.framework.helpers.PrincipalHelper;
import acme.framework.services.AbstractService;
import acme.roles.Lecturer;
import filter.SpamFilter;

@Service
public class AuthenticatedLecturerCreateService extends AbstractService<Authenticated, Lecturer> {
	// Internal state ---------------------------------------------------------

	@Autowired
	protected AuthenticatedLecturerRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		super.getResponse().setChecked(true);
	}

	@Override
	public void authorise() {
		boolean status;
		boolean isAuthenticated;
		isAuthenticated = super.getRequest().getPrincipal().isAuthenticated();
		status = !super.getRequest().getPrincipal().hasRole(Lecturer.class);
		super.getResponse().setAuthorised(status && isAuthenticated);
	}

	@Override
	public void load() {
		Lecturer object;
		Principal principal;
		int userAccountId;
		UserAccount userAccount;

		principal = super.getRequest().getPrincipal();
		userAccountId = principal.getAccountId();
		userAccount = this.repository.findUserAccountById(userAccountId);

		object = new Lecturer();
		object.setUserAccount(userAccount);
		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Lecturer object) {
		assert object != null;
		super.bind(object, "almaMater", "resume", "qualifications", "link");
	}

	@Override
	public void validate(final Lecturer object) {
		assert object != null;
		if (!super.getBuffer().getErrors().hasErrors("almaMater"))
			super.state(!SpamFilter.antiSpamFilter(object.getAlmaMater(), this.repository.findThreshold()), "almaMater", "authenticated.lecturer.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("qualifications"))
			super.state(!SpamFilter.antiSpamFilter(object.getQualifications(), this.repository.findThreshold()), "qualifications", "authenticated.lecturer.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("resume"))
			super.state(!SpamFilter.antiSpamFilter(object.getResume(), this.repository.findThreshold()), "resume", "authenticated.lecturer.error.spam");

	}

	@Override
	public void perform(final Lecturer object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Lecturer object) {
		assert object != null;
		final Tuple tuple;
		tuple = super.unbind(object, "almaMater", "resume", "qualifications", "link");
		super.getResponse().setData(tuple);
	}

	@Override
	public void onSuccess() {
		if (super.getRequest().getMethod().equals(HttpMethod.POST))
			PrincipalHelper.handleUpdate();
	}
}
