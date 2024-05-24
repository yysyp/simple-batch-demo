package ps.demo.simplebatchdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.stereotype.Component;
import ps.demo.simplebatchdemo.entity.Student;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
@Component
public class MyItemWriter extends JpaItemWriter<Student> {

    private JobParameters jobParameters;


    //private final EntityManagerFactory entityManagerFactory;

    public MyItemWriter(EntityManagerFactory entityManagerFactory) {
        //this.entityManagerFactory = entityManagerFactory;

        this.setEntityManagerFactory(entityManagerFactory);
    }


    public void setJobParameters(JobParameters jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    protected void doWrite(EntityManager entityManager, List<? extends Student> items) {
        log.info("MyItemWriter doWrite {}", jobParameters.getLong("timestamp"));
        super.doWrite(entityManager, items);
    }

}
