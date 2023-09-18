package ps.demo.simplebatchdemo.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ps.demo.simplebatchdemo.entity.TaskCache;
import ps.demo.simplebatchdemo.repository.TaskCacheRepository;

import java.util.Date;

@Service
public class TaskCacheService {
    private final TaskCacheRepository repository;

    @Autowired
    public TaskCacheService(TaskCacheRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "taskCache", key = "#paramDate.toString() + '-' + #paramSite")
    public String getCachedResult(Date paramDate, String paramSite) {
        TaskCache cacheEntity = repository.findByParamDateAndParamSite(paramDate, paramSite);
        if (cacheEntity != null) {
            return cacheEntity.getResultData();
        }
        return null;
    }

    @CacheEvict(value = "taskCache", key = "#paramDate.toString() + '-' + #paramSite")
    public void clearCache(Date paramDate, String paramSite) {
        // This method clears the cache entry for the specified param date and param site
    }
}