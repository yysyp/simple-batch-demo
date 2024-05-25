package ps.demo.simplebatchdemo.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JobListener implements JobExecutionListener {

    //private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private long startTime;

//    @Autowired
//    public JobListener(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
//        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
//    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
        String name = jobExecution.getJobConfigurationName();
        log.info("job before " +name +" " + jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String name = jobExecution.getJobConfigurationName();
        log.info("JOB STATUS : {}, {}", name, jobExecution.getStatus());
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("JOB FINISHED {}", name);
            //threadPoolTaskExecutor.destroy();
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.info("JOB FAILED {}", name);
        }
        log.info("Job Cost Time : {}/ms {}", System.currentTimeMillis() - startTime, name);
    }

}
