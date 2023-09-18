package ps.demo.simplebatchdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ps.demo.simplebatchdemo.service.TaskCacheService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class TaskController {
    
    private final TaskCacheService taskCacheService;

    @Autowired
    public TaskController(TaskCacheService taskCacheService) {
        this.taskCacheService = taskCacheService;
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

}