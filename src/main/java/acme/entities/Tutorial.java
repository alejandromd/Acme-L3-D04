
package acme.entities;

import java.time.Duration;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import acme.entities.tutorialSession.TutorialSession;
import acme.framework.data.AbstractEntity;
import acme.framework.helpers.MomentHelper;
import acme.roles.Assistant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Tutorial extends AbstractEntity {

	// Serialisation identifier -----------------------------------------------

	protected static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@NotBlank
	@Pattern(regexp = "[A-Z]{1,3}\\d{3}")
	@Column(unique = true)
	protected String			code;

	@NotBlank
	@Length(max = 75)
	protected String			title;

	@NotBlank
	@Length(max = 100)
	protected String			informativeAbstract;

	@NotBlank
	@Length(max = 100)
	protected String			goals;

	protected boolean			draftMode;

	// Derived attributes ----------------------------------------------------


	public int estimatedTime(final Collection<TutorialSession> sessions) {
		int result = 0;
		if (sessions.size() > 0)
			for (final TutorialSession session : sessions) {
				final Duration duration = MomentHelper.computeDuration(session.getStartTimestamp(), session.getEndTimestamp());
				result += (int) duration.toHours();
			}
		return result;
	}

	// Relationships ----------------------------------------------------------


	@NotNull
	@Valid
	@ManyToOne(optional = false)
	protected Assistant	assistant;

	@NotNull
	@Valid
	@ManyToOne(optional = false)
	protected Course	course;
}
