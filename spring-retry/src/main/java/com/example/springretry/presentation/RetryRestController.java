package com.example.springretry.presentation;

import com.example.springretry.application.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RetryRestController {

    private final RetryService retryService;
    private final RetryTemplate retryTemplate;

    @GetMapping("/success")
    public int success(){
        return retryService.retrySuccess();
    }

    @GetMapping("/fail")
    public int fail(){
        return retryService.retryFail("fail1");
    }

    @GetMapping("/fail2")
    public int fail2(){
        log.info("controller call");
        return retryService.retryFail2("fail2");
    }

    @GetMapping("/template")
    public int template(){
        return retryTemplate.execute(context -> retryService.retryTemplate()
                , context -> retryService.retryTemplateRecover());

//        람다식 풀어쓴 방식
//        return retryTemplate.execute(new RetryCallback<Integer, RuntimeException>() {
//                                         @Override
//                                         public Integer doWithRetry(RetryContext context) throws RuntimeException {
//                                             return retryService.retryTemplate();
//                                         }
//                                     }
//                , new RecoveryCallback<Integer>() {
//                    @Override
//                    public Integer recover(RetryContext context) throws Exception {
//                        return retryService.retryTemplateRecover();
//                    }
//                });
    }
}
