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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class SecondJob {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory emf;

    private final JobListener jobListener;

    private final StepListener stepListener;

    private final ProcessListener processListener;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor1;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor2;

    private final PlatformTransactionManager batchTransactionManager;

    public SecondJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                     EntityManagerFactory emf, JobListener jobListener,
                     @Qualifier("threadPoolTaskExecutor1") ThreadPoolTaskExecutor threadPoolTaskExecutor1,
                     @Qualifier("threadPoolTaskExecutor2") ThreadPoolTaskExecutor threadPoolTaskExecutor2,
                     StepListener stepListener, ProcessListener processListener,
                     PlatformTransactionManager batchTransactionManager) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.emf = emf;
        this.jobListener = jobListener;
        this.threadPoolTaskExecutor1 = threadPoolTaskExecutor1;
        this.threadPoolTaskExecutor2 = threadPoolTaskExecutor2;
        this.stepListener = stepListener;
        this.processListener = processListener;
        this.batchTransactionManager = batchTransactionManager;
    }

    public Job secondJob() {

        return jobBuilderFactory.get("secondJob").
                incrementer(new RunIdIncrementer()).
                //JOB执行的第一个step
                        start(secondJobStep()).
                // 调用next方法设置其他的step
                // next(xxxStep()).
                // ...
                // JobListener
                        listener(jobListener).
                        build();
    }

    private Step secondJobStep() {
        //myItemWriter.setEntityManagerFactory(emf);

        return stepBuilderFactory.get("secondJob-getData").
                //listener(stepListener).
                //listener(processListener).
                //chunk的含义就是：逐条的(Read)，等凑齐chunk数量后再对这一批进行(Process)，然后等process凑齐chunk数量后，再对这一批进行(Write)
                        <Student, Student>chunk(10).//, batchTransactionManager).
                // 捕捉到异常就重试,重试100次还是异常,JOB就停止并标志失败
                        faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(0).skip(Exception.class).
                        reader(getDataReader()).
                        //reader(getMockDataReader()).
                        //reader(getMockDataReader()).
                        processor(getDataProcessor()).
                        writer(getDataWriter()).
                        //writer(myItemWriter).
                        taskExecutor(threadPoolTaskExecutor2).
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
        final int total = 33;
        SecondJob secondJob = this;
        return new ItemReader<Student>() {
            volatile long id = 0L;

            @Override
            public Student read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                synchronized (secondJob) {
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
                log.info("--second read data : " + student.toString());
                Thread.sleep(RandomUtils.nextInt(1, 1000));
                return student;
            }
        };
    }

    private ItemProcessor<Student, Student> getDataProcessor() {
        return student -> {
            log.info("seconds Job xxx process data : " + student.toString());
            Thread.sleep(RandomUtils.nextInt(1, 1000));
            return student;
        };
    }

    private ItemWriter<Student> getDataWriter() {
        return list -> {
            log.info("----->>seconds Job write one batch list.size={}", list.size());
            for (Student student : list) {
                log.info("seconds Job write data : " + student);
                Thread.sleep(RandomUtils.nextInt(1, 1000));
            }
        };
    }


}
