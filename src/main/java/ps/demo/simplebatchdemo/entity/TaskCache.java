package ps.demo.simplebatchdemo.entity;

import lombok.Data;
import org.springframework.cache.annotation.Cacheable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity(name = "task_cache")
//@Cacheable("task_cache")
public class TaskCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date paramDate;
    private String paramSite;
    private String resultData;

}
