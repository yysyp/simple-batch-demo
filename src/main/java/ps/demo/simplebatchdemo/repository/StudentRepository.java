package ps.demo.simplebatchdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ps.demo.simplebatchdemo.entity.Student;


public interface StudentRepository extends JpaRepository<Student, Long> {

}
