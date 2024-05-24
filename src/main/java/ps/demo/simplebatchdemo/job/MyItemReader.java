package ps.demo.simplebatchdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import ps.demo.simplebatchdemo.entity.Student;
@Slf4j
@Component
public class MyItemReader implements ItemReader<Student> {
    volatile long id = 0L;

    int total = 43;

    private JobParameters jobParameters;

    public void setJobParameters(JobParameters jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public Student read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        synchronized (this) {
            if (id >= total) {
                //id = 0L;
                return null;
            }
            id++;
        }
        Long parTs = jobParameters.getLong("timestamp");
        log.info("--MyItemReader par ts={}", parTs);
        Student student = new Student();
        student.setId(id);
        student.setFirstName(RandomStringUtils.randomAlphabetic(6));
        student.setLastName(RandomStringUtils.randomAlphabetic(4));
        log.info("--read data : " + student.toString());
        Thread.sleep(RandomUtils.nextInt(1, 1000));
        return student;
    }
}
