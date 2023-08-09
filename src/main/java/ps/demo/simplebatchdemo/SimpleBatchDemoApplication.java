package ps.demo.simplebatchdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import ps.demo.simplebatchdemo.job.DataBatchJob;

//@EnableScheduling
@Slf4j
@EnableBatchProcessing
@SpringBootApplication
public class SimpleBatchDemoApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SimpleBatchDemoApplication.class, args);
    }

    private final JobLauncher jobLauncher;
    private final DataBatchJob dataBatchJob;

    @Autowired
    public SimpleBatchDemoApplication(JobLauncher jobLauncher, DataBatchJob dataBatchJob) {
        this.jobLauncher = jobLauncher;
        this.dataBatchJob = dataBatchJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Job to start.");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        Job job = dataBatchJob.dataHandleJob();
        JobExecution execution = jobLauncher.run(job, jobParameters);
        log.info("Job end status : {}", execution.getStatus());
    }
}
