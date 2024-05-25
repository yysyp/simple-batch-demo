package ps.demo.simplebatchdemo.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ps.demo.simplebatchdemo.job.MyItemReader;
import ps.demo.simplebatchdemo.job.MyItemWriter;

@Slf4j
@Component
public class StepListener implements StepExecutionListener {


    @Autowired
    private MyItemReader myItemReader;

    @Autowired
    private MyItemWriter myItemWriter;


    @Override
    public void beforeStep(StepExecution stepExecution) {
        JobParameters jobParameters = stepExecution.getJobParameters();
        myItemReader.setJobParameters(jobParameters);
        myItemWriter.setJobParameters(jobParameters);
        String stepName = stepExecution.getStepName();
        log.info("Before step == {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("After step == {}", stepExecution.getStepName());
        return stepExecution.getExitStatus();
    }
}
