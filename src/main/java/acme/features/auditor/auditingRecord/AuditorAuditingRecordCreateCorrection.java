
package acme.features.auditor.auditingRecord;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Audit;
import acme.entities.auditingRecord.AuditingRecord;
import acme.entities.auditingRecord.Mark;
import acme.framework.components.accounts.Principal;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Auditor;
import filter.SpamFilter;

@Service
public class AuditorAuditingRecordCreateCorrection extends AbstractService<Auditor, AuditingRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected AuditorAuditingRecordRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("masterId", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		boolean status;
		int masterId;
		Audit audit;
		int userId;
		Principal principal;

		masterId = super.getRequest().getData("masterId", int.class);
		audit = this.repository.findOneAuditById(masterId);
		principal = super.getRequest().getPrincipal();
		userId = principal.getAccountId();
		status = !audit.isDraftMode() && audit.getAuditor().getUserAccount().getId() == userId;

		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		AuditingRecord object;
		int masterId;
		Audit audit;

		masterId = super.getRequest().getData("masterId", int.class);
		audit = this.repository.findOneAuditById(masterId);

		object = new AuditingRecord();
		object.setCorrection(true);
		object.setAudit(audit);

		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final AuditingRecord object) {
		assert object != null;

		super.bind(object, "subject", "assessment", "periodStartDate", "periodEndDate", "mark", "link");
	}

	@Override
	public void validate(final AuditingRecord object) {
		assert object != null;

		boolean confirmation;

		if (!super.getBuffer().getErrors().hasErrors("periodEndDate") && !super.getBuffer().getErrors().hasErrors("periodStartDate")) {
			Date minimumPeriod;

			minimumPeriod = MomentHelper.deltaFromMoment(object.getPeriodStartDate(), 1, ChronoUnit.HOURS);
			super.state(MomentHelper.isAfterOrEqual(object.getPeriodEndDate(), minimumPeriod), "periodEndDate", "auditor.auditing-record.form.error.too-close");
		}

		confirmation = super.getRequest().getData("confirmation", boolean.class);
		super.state(confirmation, "confirmation", "javax.validation.constraints.AssertTrue.message");
		if (!super.getBuffer().getErrors().hasErrors("subject"))
			super.state(!SpamFilter.antiSpamFilter(object.getSubject(), this.repository.findThreshold()), "subject", "auditor.auditing-record.form.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("assessment"))
			super.state(!SpamFilter.antiSpamFilter(object.getAssessment(), this.repository.findThreshold()), "assessment", "auditor.auditing-record.form.error.spam");
	}

	@Override
	public void perform(final AuditingRecord object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final AuditingRecord object) {
		assert object != null;

		Tuple tuple;
		SelectChoices choices;
		choices = SelectChoices.from(Mark.class, object.getMark());

		tuple = super.unbind(object, "subject", "assessment", "periodStartDate", "periodEndDate", "link");
		tuple.put("masterId", super.getRequest().getData("masterId", int.class));
		tuple.put("draftMode", object.getAudit().isDraftMode());
		tuple.put("confirmation", false);
		tuple.put("marks", choices);
		tuple.put("mark", choices.getSelected().getKey());

		super.getResponse().setData(tuple);
	}

}
