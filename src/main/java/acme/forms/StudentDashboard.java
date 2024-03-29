
package acme.forms;

import acme.framework.data.AbstractForm;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentDashboard extends AbstractForm {

	protected static final long	serialVersionUID	= 1L;

	Integer						totalActivities;

	Double						averagePeriodOfActivities;
	Double						deviationPeriodOfActivities;
	Double						minimumPeriodOfActivities;
	Double						maximumPeriodOfActivities;

	Double						averageLearningTime;
	Double						devitationLearningTime;
	Double						minimumLearningTime;
	Double						maximunLearningTime;

}
