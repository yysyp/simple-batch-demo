package ps.demo.simplebatchdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import ps.demo.simplebatchdemo.entity.Student;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class MyItemReader implements ItemReader<Student> {
    volatile long id = 0L;

    int total = 53;

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
        student.setEmail(RandomStringUtils.randomAlphabetic(5)+"@test.com");
        student.setPhone(RandomStringUtils.randomNumeric(11));
        student.setAddress(RandomStringUtils.randomAlphabetic(15));
        student.setCity(List.of("SH", "GZ", "BJ").get(RandomUtils.nextInt(0, 3)));
        student.setState("CN");
        student.setZip(RandomStringUtils.randomNumeric(5));
        student.setDateOfBirth(DateUtils.addYears(new Date(), -RandomUtils.nextInt(10, 80)));
        student.setGpa(RandomUtils.nextDouble(0, 10));
        student.setState("1");
        log.info("--read data : " + student.toString());
        Thread.sleep(RandomUtils.nextInt(1, 1000));
        return student;
    }
}
