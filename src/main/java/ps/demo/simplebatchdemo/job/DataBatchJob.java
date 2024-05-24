package ps.demo.simplebatchdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import ps.demo.simplebatchdemo.entity.Student;
import ps.demo.simplebatchdemo.job.listener.JobListener;
import ps.demo.simplebatchdemo.job.listener.ProcessListener;
import ps.demo.simplebatchdemo.job.listener.StepListener;

import javax.persistence.EntityManagerFactory;


@Slf4j
@Component
public class DataBatchJob {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory emf;

    private final JobListener jobListener;

    private final StepListener stepListener;

    private final ProcessListener processListener;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private final PlatformTransactionManager batchTransactionManager;

    public DataBatchJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                        EntityManagerFactory emf, JobListener jobListener, ThreadPoolTaskExecutor threadPoolTaskExecutor,
                        StepListener stepListener, ProcessListener processListener,
                        PlatformTransactionManager batchTransactionManager) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.emf = emf;
        this.jobListener = jobListener;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.stepListener = stepListener;
        this.processListener = processListener;
        this.batchTransactionManager = batchTransactionManager;
    }

    public Job dataHandleJob() {

        return jobBuilderFactory.get("dataHandleJob").
                incrementer(new RunIdIncrementer()).
                //JOB执行的第一个step
                        start(handleDataStep()).
                // 调用next方法设置其他的step
                // next(xxxStep()).
                // ...
                // JobListener
                        listener(jobListener).
                        build();
    }


    private Step handleDataStep() {
        return stepBuilderFactory.get("getData").
                listener(stepListener).
                //listener(processListener).
                //chunk的含义就是：逐条的(Read)，等凑齐chunk数量后再对这一批进行(Process)，然后等process凑齐chunk数量后，再对这一批进行(Write)
                        <Student, Student>chunk(10).//, batchTransactionManager).
                // 捕捉到异常就重试,重试100次还是异常,JOB就停止并标志失败
                        faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(100).skip(Exception.class).
                        //reader(getDataReader()).
                        reader(getMockDataReader()).
                        processor(getDataProcessor()).
                        writer(getDataWriter()).
                        taskExecutor(threadPoolTaskExecutor).
                        throttleLimit(10).
                        build();
    }

    private ItemReader<? extends Student> getDataReader() {
        JpaPagingItemReader<Student> reader = new JpaPagingItemReader<>();
        try {
            JpaNativeQueryProvider<Student> queryProvider = new JpaNativeQueryProvider<>();
            queryProvider.setSqlQuery("SELECT * FROM student");
            queryProvider.setEntityClass(Student.class);
            queryProvider.afterPropertiesSet();

            reader.setEntityManagerFactory(emf);
            reader.setPageSize(3);
            reader.setQueryProvider(queryProvider);
            reader.afterPropertiesSet();

            // 所有ItemReader和ItemWriter实现都会在ExecutionContext提交之前将其当前状态存储在其中,
            // 如果不希望这样做,可以设置setSaveState(false)
            reader.setSaveState(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reader;
    }

    private ItemReader<? extends Student> getMockDataReader() {
        final int total = 43;
        DataBatchJob thisHolder = this;
        return new ItemReader<Student>() {
            volatile long id = 0L;

            @Override
            public Student read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                synchronized (thisHolder) {
                    if (id >= total) {
                        //id = 0L;
                        return null;
                    }
                    id++;
                }

                Student student = new Student();
                student.setId(id);
                student.setFirstName(RandomStringUtils.randomAlphabetic(6));
                student.setLastName(RandomStringUtils.randomAlphabetic(4));
                log.info("--read data : " + student.toString());
                Thread.sleep(RandomUtils.nextInt(1, 1000));
                return student;
            }
        };
    }

    private ItemProcessor<Student, Student> getDataProcessor() {
        return student -> {
            log.info("xxx process data : " + student.toString());
            Thread.sleep(RandomUtils.nextInt(1, 1000));
            return student;
        };
    }

    private ItemWriter<Student> getDataWriter() {
        return list -> {
            log.info("----->>write one batch list.size={}", list.size());
            for (Student student : list) {
                log.info("write data : " + student);
                Thread.sleep(RandomUtils.nextInt(1, 1000));
            }
        };
    }


}
