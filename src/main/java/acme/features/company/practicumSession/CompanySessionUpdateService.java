
package acme.features.company.practicumSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.PracticumSession;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Company;

@Service
public class CompanySessionUpdateService extends AbstractService<Company, PracticumSession> {

	@Autowired
	protected CompanySessionRepository repository;


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		boolean status;
		int sessionId;
		PracticumSession session;

		sessionId = super.getRequest().getData("id", int.class);
		session = this.repository.findOneSessionById(sessionId);
		status = session != null && session.getPracticum().getDraftMode() && super.getRequest().getPrincipal().hasRole(session.getPracticum().getCompany())
			&& super.getRequest().getPrincipal().getUsername().equals(session.getPracticum().getCompany().getUserAccount().getUsername());

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		PracticumSession object;
		int sessionId;

		sessionId = super.getRequest().getData("id", int.class);
		object = this.repository.findOneSessionById(sessionId);

		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final PracticumSession object) {
		assert object != null;

		super.bind(object, "title", "summary", "initialPeriod", "finalPeriod", "link");
	}

	@Override
	public void validate(final PracticumSession object) {
		assert object != null;
		if (!super.getBuffer().getErrors().hasErrors("initialPeriod") && !super.getBuffer().getErrors().hasErrors("endTime"))
			if (!MomentHelper.isBefore(object.getInitialPeriod(), object.getFinalPeriod()))
				super.state(false, "finalPeriod", "company.session.form.error.end-before-start");
			else {
				final int days = (int) MomentHelper.computeDuration(MomentHelper.getCurrentMoment(), object.getInitialPeriod()).toDays();
				if (days < 7)
					super.state(false, "initialPeriod", "company.practicum-session.form.error.start-period");
				else {
					final int duracion = (int) MomentHelper.computeDuration(object.getInitialPeriod(), object.getFinalPeriod()).toDays();
					if (duracion < 7)
						super.state(false, "finalPeriod", "company.practicum-session.form.error.end-period");
				}
			}

	}

	@Override
	public void perform(final PracticumSession object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final PracticumSession object) {
		assert object != null;

		Tuple tuple;

		tuple = super.unbind(object, "title", "summary", "initialPeriod", "finalPeriod", "link");
		tuple.put("draftMode", object.getPracticum().getDraftMode());

		super.getResponse().setData(tuple);
	}

}