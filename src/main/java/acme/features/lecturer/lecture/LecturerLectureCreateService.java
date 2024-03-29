
package acme.features.lecturer.lecture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.datatypes.Nature;
import acme.entities.Lecture;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Lecturer;
import filter.SpamFilter;

@Service
public class LecturerLectureCreateService extends AbstractService<Lecturer, Lecture> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected LecturerLectureRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		super.getResponse().setChecked(true);
	}

	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Lecture object;
		Lecturer lecturer;

		object = new Lecture();
		object.setDraftMode(true);
		lecturer = this.repository.findLecturerById(super.getRequest().getPrincipal().getActiveRoleId());
		object.setLecturer(lecturer);
		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Lecture object) {
		assert object != null;
		super.bind(object, "title", "summary", "estimatedLearningTime", "body", "lectureType", "link");
	}

	@Override
	public void validate(final Lecture object) {
		assert object != null;
		if (!super.getBuffer().getErrors().hasErrors("title")) {
			Lecture lecture;

			lecture = this.repository.findLectureByTitle(object.getTitle(), object.getLecturer());
			super.state(lecture == null, "title", "lecturer.lecture.form.error.title-duplicated");
		}
		if (!super.getBuffer().getErrors().hasErrors("estimatedLearningTime"))
			super.state(object.getEstimatedLearningTime() >= 0.01, "estimatedLearningTime", "lecturer.lecture.form.error.estimatedLearningTime");
		if (!super.getBuffer().getErrors().hasErrors("summary"))
			super.state(!SpamFilter.antiSpamFilter(object.getSummary(), this.repository.findThreshold()), "summary", "lecturer.lecture.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("title"))
			super.state(!SpamFilter.antiSpamFilter(object.getTitle(), this.repository.findThreshold()), "title", "lecturer.lecture.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("body"))
			super.state(!SpamFilter.antiSpamFilter(object.getBody(), this.repository.findThreshold()), "body", "lecturer.lecture.error.spam");

	}

	@Override
	public void perform(final Lecture object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Lecture object) {
		assert object != null;
		Tuple tuple;

		tuple = super.unbind(object, "title", "summary", "estimatedLearningTime", "body", "lectureType", "link", "draftMode", "lecturer");
		final SelectChoices choices;
		choices = SelectChoices.from(Nature.class, object.getLectureType());
		tuple.put("lectureType", choices.getSelected().getKey());
		tuple.put("lectureTypes", choices);
		super.getResponse().setData(tuple);
	}
}
