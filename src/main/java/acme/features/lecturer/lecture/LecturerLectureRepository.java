
package acme.features.lecturer.lecture;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.entities.Course;
import acme.entities.CourseLecture;
import acme.entities.Lecture;
import acme.framework.repositories.AbstractRepository;
import acme.roles.Lecturer;

@Repository
public interface LecturerLectureRepository extends AbstractRepository {

	@Query("select c from Course c where c.id = :id")
	Course findCourseById(int id);

	@Query("select l from Lecture l where l.id = :id")
	Lecture findLectureById(int id);

	@Query("select l from Lecturer l where l.id = :id")
	Lecturer findLecturerById(int id);

	@Query("select l from Lecture l inner join CourseLecture cl on l = cl.lecture inner join Course c on cl.course = c where c.id = :id")
	Collection<Lecture> findLecturesByCourse(int id);

	@Query("select l from Lecture l where l.lecturer = :lecturer")
	Collection<Lecture> findLecturesByLecturer(Lecturer lecturer);

	@Query("select c from Course c inner join CourseLecture cl on c = cl.course inner join Lecture l on cl.lecture = l where l = :lecture")
	Collection<Course> findCourseByLecture(Lecture lecture);

	@Query("select cl from CourseLecture cl where cl.lecture = :lecture")
	Collection<CourseLecture> findCourseLecturesByLecture(Lecture lecture);

	@Query("select l from Lecturer l where l.userAccount.id = :id")
	Lecturer findLecturerByIdUserAccount(int id);

	@Query("select sc.threshold from SpamConfig sc")
	Double findThreshold();

	@Query("select l from Lecture l where l.title = :title and l.lecturer = :lecturer")
	Lecture findLectureByTitle(String title, Lecturer lecturer);
}
