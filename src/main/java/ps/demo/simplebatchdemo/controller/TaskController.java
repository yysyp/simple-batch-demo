package ps.demo.simplebatchdemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ps.demo.simplebatchdemo.job.DataBatchJob;
import ps.demo.simplebatchdemo.service.TaskCacheService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api")
public class TaskController {
    
    private final TaskCacheService taskCacheService;

    private final JobLauncher jobLauncher;
    private final DataBatchJob dataBatchJob;

    @Autowired
    public TaskController(TaskCacheService taskCacheService, JobLauncher jobLauncher, DataBatchJob dataBatchJob) {
        this.taskCacheService = taskCacheService;
        this.jobLauncher = jobLauncher;
        this.dataBatchJob = dataBatchJob;
    }

    @GetMapping("/data/{paramDate}/{paramSite}")
    public String getData(@PathVariable("paramDate") String paramDate,
                          @PathVariable("paramSite") String paramSite) {
        // Parse the paramDate string to a Date object
        Date parsedParamDate = getParsedParamDate(paramDate);

        // Check if the result is cached
        String cachedResult = taskCacheService.getCachedResult(parsedParamDate, paramSite);
        return cachedResult;

    }

    private Date getParsedParamDate(String paramDate) {
        Date parsedParamDate;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            parsedParamDate = dateFormat.parse(paramDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid query date format. Please use yyyy-MM-dd format.");
        }
        return parsedParamDate;
    }

    @DeleteMapping("/data/{paramDate}/{paramSite}")
    public String delDataCache(@PathVariable("paramDate") String paramDate,
                          @PathVariable("paramSite") String paramSite) {
        Date parsedParamDate = getParsedParamDate(paramDate);
        taskCacheService.clearCache(parsedParamDate, paramSite);
        return "Success";
    }


    @GetMapping("/trigger/{key}")
    public String trigger(@PathVariable("key") String key
                          ) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        log.info("------------------------>>Trigger job to run...");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("key", key)
                .toJobParameters();

        Job job = dataBatchJob.dataHandleJob();
        JobExecution execution = jobLauncher.run(job, jobParameters);
        log.info("------------------------>>Job end. Exit Status : {}", execution.getStatus());
        return execution.getStatus().name();
    }

}