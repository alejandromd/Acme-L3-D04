
package acme.features.student.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.datatypes.Nature;
import acme.entities.Activity;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Student;
import filter.SpamFilter;

@Service
public class ActivityPublishService extends AbstractService<Student, Activity> {

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
		boolean status;
		Activity object;

		final int id = super.getRequest().getData("id", int.class);
		object = this.repository.findActivityById(id);
		status = object.getEnrolment() != null && !object.getEnrolment().getDraftMode() && object.getDraftMode() && super.getRequest().getPrincipal().hasRole(object.getEnrolment().getStudent());

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Activity object;

		final int id = super.getRequest().getData("id", int.class);
		object = this.repository.findActivityById(id);

		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Activity object) {
		assert object != null;

		super.bind(object, "title", "summary", "activityType", "startPeriod", "endPeriod", "link");
	}

	@Override
	public void validate(final Activity object) {
		assert object != null;

		if (!super.getBuffer().getErrors().hasErrors("endPeriod"))
			super.state(MomentHelper.isBefore(object.getStartPeriod(), object.getEndPeriod()), "endPeriod", "student.activity.form.error.wrong-dates");
		if (!super.getBuffer().getErrors().hasErrors("title"))
			super.state(!SpamFilter.antiSpamFilter(object.getTitle(), this.repository.findThreshold()), "title", "student.activity.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("summary"))
			super.state(!SpamFilter.antiSpamFilter(object.getSummary(), this.repository.findThreshold()), "summary", "student.activity.error.spam");
	}

	@Override
	public void perform(final Activity object) {
		assert object != null;
		object.setDraftMode(false);
		this.repository.save(object);
	}

	@Override
	public void unbind(final Activity object) {
		assert object != null;

		Tuple tuple;

		final SelectChoices s = SelectChoices.from(Nature.class, object.getActivityType());

		tuple = super.unbind(object, "title", "summary", "startPeriod", "endPeriod", "link", "draftMode");
		tuple.put("enrolmentId", object.getEnrolment().getId());
		tuple.put("activityTypes", s);
		tuple.put("activityType", s.getSelected().getKey());

		super.getResponse().setData(tuple);
	}

}
