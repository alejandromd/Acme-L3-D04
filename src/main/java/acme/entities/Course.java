
package acme.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import acme.datatypes.Nature;
import acme.framework.components.datatypes.Money;
import acme.framework.data.AbstractEntity;
import acme.roles.Lecturer;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Course extends AbstractEntity {

	// Serialisation identifier -----------------------------------------------

	protected static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Column(unique = true)
	@NotBlank
	@Pattern(regexp = "[A-Z]{1,3}\\d{3}")
	protected String			code;

	@NotBlank
	@Length(max = 75)
	protected String			title;

	@NotBlank
	@Length(max = 100)
	protected String			summary;

	@NotNull
	protected Money				retailPrice;

	protected boolean			draftMode;

	@URL
	protected String			link;

	// Relationships ----------------------------------------------------------

	@ManyToOne(optional = false)
	@NotNull
	@Valid
	protected Lecturer			lecturer;


	// Theoretical courses should be rejected
	public String courseTypeNature(final List<Lecture> lectures) {
		String courseType = "BALANCED";
		if (!lectures.isEmpty()) {
			int theoretical = 0;
			int handsOn = 0;
			for (final Lecture l : lectures)
				if (l.getLectureType().equals(Nature.THEORETICAL))
					theoretical++;
				else if (l.getLectureType().equals(Nature.HANDS_ON))
					handsOn++;
			if (theoretical > handsOn)
				courseType = "THEORETICAL";
			else if (handsOn > theoretical)
				courseType = "HANDS_ON";
		}
		return courseType;
	}

}
