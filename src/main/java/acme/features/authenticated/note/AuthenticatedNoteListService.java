
package acme.features.authenticated.note;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Note;
import acme.framework.components.accounts.Authenticated;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;

@Service
public class AuthenticatedNoteListService extends AbstractService<Authenticated, Note> {

	// Internal state ---------------------------------------------------------
	@Autowired
	protected AuthenticatedNoteRepository repository;


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

		Collection<Note> objects;
		Date deadline;

		deadline = MomentHelper.deltaFromCurrentMoment(-30, ChronoUnit.DAYS);
		objects = this.repository.findRecentNotes(deadline);

		super.getBuffer().setData(objects);

	}

	@Override
	public void unbind(final Note object) {

		assert object != null;

		Tuple tuple;
		String payload;

		tuple = super.unbind(object, "instantiationMoment", "title", "author");
		payload = String.format("%s;%s;%s", object.getMessage(), object.getEmail(), object.getLink());
		tuple.put("payload", payload);
		super.getResponse().setData(tuple);

	}

}
