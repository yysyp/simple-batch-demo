package ps.demo.simplebatchdemo.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.orm.JpaNativeQueryProvider;
import org.springframework.stereotype.Component;
import ps.demo.simplebatchdemo.entity.Student;
import ps.demo.simplebatchdemo.job.listener.JobListener;

import javax.persistence.EntityManagerFactory;


@Slf4j
@Component
public class DataBatchJob {

    /**
     * Job构建工厂，用于构建Job
     */
    private final JobBuilderFactory jobBuilderFactory;

    /**
     * Step构建工厂，用于构建Step
     */
    private final StepBuilderFactory stepBuilderFactory;

    /**
     * 实体类管理工工厂，用于访问表格数据
     */
    private final EntityManagerFactory emf;

    /**
     * 自定义的简单Job监听器
     */
    private final JobListener jobListener;

    public DataBatchJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                        EntityManagerFactory emf, JobListener jobListener) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.emf = emf;
        this.jobListener = jobListener;
    }

    /**
     * 一个最基础的Job通常由一个或者多个Step组成
     */
    public Job dataHandleJob() {

        return jobBuilderFactory.get("dataHandleJob").
                incrementer(new RunIdIncrementer()).
                // start是JOB执行的第一个step
                        start(handleDataStep()).
                // 可以调用next方法设置其他的step，例如：
                // next(xxxStep()).
                // next(xxxStep()).
                // ...
                // 设置我们自定义的JobListener
                        listener(jobListener).
                        build();
    }


    /**
     * 一个简单基础的Step主要分为三个部分
     * ItemReader : 用于读取数据
     * ItemProcessor : 用于处理数据
     * ItemWriter : 用于写数据
     *
     * @return
     */
    private Step handleDataStep() {
        return stepBuilderFactory.get("getData").
                // <输入对象, 输出对象>  chunk通俗的讲类似于SQL的commit; 这里表示处理(processor)100条后写入(writer)一次
                        <Student, Student>chunk(100).
                // 捕捉到异常就重试,重试100次还是异常,JOB就停止并标志失败
                        faultTolerant().retryLimit(3).retry(Exception.class).skipLimit(100).skip(Exception.class).
                // 指定ItemReader对象
                        reader(getDataReader()).
                // 指定ItemProcessor对象
                        processor(getDataProcessor()).
                // 指定ItemWriter对象
                        writer(getDataWriter()).
                        build();
    }

    /**
     * 读取数据
     *
     * @return ItemReader Object
     */
    private ItemReader<? extends Student> getDataReader() {
        // 读取数据,这里可以用JPA,JDBC,JMS 等方式读取数据
        JpaPagingItemReader<Student> reader = new JpaPagingItemReader<>();
        try {
            // 这里选择JPA方式读取数据
            JpaNativeQueryProvider<Student> queryProvider = new JpaNativeQueryProvider<>();
            // 一个简单的 native SQL
            queryProvider.setSqlQuery("SELECT * FROM student");
            // 设置实体类
            queryProvider.setEntityClass(Student.class);
            queryProvider.afterPropertiesSet();

            reader.setEntityManagerFactory(emf);
            // 设置每页读取的记录数
            reader.setPageSize(3);
            // 设置数据提供者
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

    /**
     * 处理数据
     *
     * @return ItemProcessor Object
     */
    private ItemProcessor<Student, Student> getDataProcessor() {
        return student -> {
            // 模拟处理数据，这里处理就是打印一下
            log.info("processor data : " + student.toString());

            return student;
        };
    }

    /**
     * 写入数据
     *
     * @return ItemWriter Object
     */
    private ItemWriter<Student> getDataWriter() {
        return list -> {
            for (Student student : list) {
                // 模拟写数据，为了演示的简单就不写入数据库了
                log.info("write data : " + student);
            }
        };
    }


}
