
package acme.features.administrator.banner;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Banner;
import acme.framework.components.accounts.Administrator;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import filter.SpamFilter;

@Service
public class AdministratorBannerUpdateService extends AbstractService<Administrator, Banner> {

	@Autowired
	protected AdministratorBannerRepository repository;


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);

		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		int masterId;
		masterId = super.getRequest().getData("id", int.class);
		final Banner banner = this.repository.findBannerById(masterId);
		final Date date = MomentHelper.getCurrentMoment();
		final boolean bool = banner.getDisplayPeriodBegin().before(date) && banner.getDisplayPeriodFinish().after(date);
		super.getResponse().setAuthorised(!bool);
	}

	@Override
	public void load() {
		Banner object;

		final int id = super.getRequest().getData("id", int.class);
		object = this.repository.findBannerById(id);

		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Banner object) {
		assert object != null;

		super.bind(object, "instantiationMoment", "slogan", "displayPeriodBegin", "displayPeriodFinish", "picture", "linkWeb");
	}

	@Override
	public void validate(final Banner object) {
		assert object != null;

		if (object.getDisplayPeriodBegin() != null)
			if (!super.getBuffer().getErrors().hasErrors("displayPeriodBegin"))
				super.state(MomentHelper.isFuture(object.getDisplayPeriodBegin()), "displayPeriodBegin", "administrator.banner.form.error.wrong-displayStart");

		if (object.getDisplayPeriodBegin() != null && object.getDisplayPeriodFinish() != null)
			if (!super.getBuffer().getErrors().hasErrors("displayPeriodBegin")) {
				Date date;
				date = MomentHelper.getCurrentMoment();
				super.state(!(date.after(object.getDisplayPeriodBegin()) && date.before(object.getDisplayPeriodFinish())), "displayPeriodBegin", "administrator.banner.form.error.wrong-update");
			}

		if (object.getDisplayPeriodBegin() != null && object.getDisplayPeriodFinish() != null)
			if (!super.getBuffer().getErrors().hasErrors("displayPeriodFinish")) {
				Date start;
				Date startOneWeek;
				Calendar calendar;

				start = object.getDisplayPeriodBegin();
				calendar = Calendar.getInstance();
				calendar.setTime(start);
				calendar.add(Calendar.WEEK_OF_YEAR, 1);
				startOneWeek = calendar.getTime();

				super.state(MomentHelper.isAfterOrEqual(object.getDisplayPeriodFinish(), startOneWeek), "displayPeriodFinish", "administrator.banner.form.error.wrong-displayEnd");
			}
		if (!super.getBuffer().getErrors().hasErrors("slogan"))
			super.state(!SpamFilter.antiSpamFilter(object.getSlogan(), this.repository.findThreshold()), "slogan", "administrator.banner.error.spam");
	}

	@Override
	public void perform(final Banner object) {
		assert object != null;

		Date d;

		d = MomentHelper.getCurrentMoment();
		object.setInstantiationMoment(d);
		this.repository.save(object);
	}

	@Override
	public void unbind(final Banner object) {
		assert object != null;
		Tuple tuple;

		tuple = super.unbind(object, "instantiationMoment", "slogan", "displayPeriodBegin", "displayPeriodFinish", "picture", "linkWeb");
		super.getResponse().setGlobal("isViewable", true);
		super.getResponse().setData(tuple);
	}

}
