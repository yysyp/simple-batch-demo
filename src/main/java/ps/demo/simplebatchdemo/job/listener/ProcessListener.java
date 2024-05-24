package ps.demo.simplebatchdemo.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcessListener<T, S> implements ItemProcessListener<T, S> {

    @Override
    public void beforeProcess(T t) {
        log.info("Before process item={}", t);
    }

    @Override
    public void afterProcess(T item, S result) {
        log.info("After process item {} having result {}", item, result);
    }

    @Override
    public void onProcessError(T t, Exception e) {
        log.info("processing item {}", t);

    }
}
