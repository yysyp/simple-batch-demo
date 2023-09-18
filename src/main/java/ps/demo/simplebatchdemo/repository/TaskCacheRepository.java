package ps.demo.simplebatchdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.stereotype.Repository;
import ps.demo.simplebatchdemo.entity.TaskCache;

import javax.persistence.TemporalType;
import java.util.Date;

@Repository
public interface TaskCacheRepository extends JpaRepository<TaskCache, Long> {

    TaskCache findByParamDateAndParamSite(@Temporal(TemporalType.DATE) Date paramDate, String paramSite);

}