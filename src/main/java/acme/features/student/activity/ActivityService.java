
package acme.features.student.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.datatypes.Nature;
import acme.entities.Activity;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Student;

@Service
public class ActivityService extends AbstractService<Student, Activity> {

	@Autowired
	protected ActivityRepository repository;


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		Activity object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findActivityById(id);
		final int userId = super.getRequest().getPrincipal().getAccountId();

		super.getResponse().setAuthorised(!object.getEnrolment().getDraftMode() && object.getEnrolment().getStudent().getUserAccount().getId() == userId);
	}

	@Override
	public void load() {
		Activity object;

		final int id = super.getRequest().getData("id", int.class);
		object = this.repository.findActivityById(id);

		super.getBuffer().setData(object);
	}

	@Override
	public void unbind(final Activity object) {
		assert object != null;

		SelectChoices s;
		Tuple tuple;

		s = SelectChoices.from(Nature.class, object.getActivityType());

		tuple = super.unbind(object, "title", "summary", "startPeriod", "endPeriod", "link", "draftMode");
		tuple.put("enrolment", object.getEnrolment().getId());
		tuple.put("activityTypes", s);
		tuple.put("activityType", s.getSelected().getKey());

		super.getResponse().setData(tuple);
	}

}
