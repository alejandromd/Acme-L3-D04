
package acme.features.authenticated.course;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import acme.entities.Course;
import acme.framework.components.accounts.Authenticated;
import acme.framework.controllers.AbstractController;

@Controller
public class AuthenticatedCourseController extends AbstractController<Authenticated, Course> {

	@Autowired
	protected AuthenticatedCourseFindAllService	findAllService;

	@Autowired
	protected AuthenticatedCourseService		showService;


	@PostConstruct
	protected void initialise() {
		super.addBasicCommand("list", this.findAllService);
		super.addBasicCommand("show", this.showService);
	}

}