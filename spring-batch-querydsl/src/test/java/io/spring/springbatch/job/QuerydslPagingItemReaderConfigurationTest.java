package io.spring.springbatch.job;

import io.spring.springbatch.TestBatchConfig;
import io.spring.springbatch.customer.Customer;
import io.spring.springbatch.customer.CustomerRepository;
import org.junit.After;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringBootTest(classes = {QuerydslPagingItemReaderConfiguration.class, TestBatchConfig.class})
public class QuerydslPagingItemReaderConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private CustomerRepository customerRepository;

    @After
    void tearDown(){
        customerRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("simple job success")
    void simpleJob_success() throws Exception{
        // given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "backtony")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED,jobExecution.getStatus());
        assertEquals(ExitStatus.COMPLETED,jobExecution.getExitStatus());
        assertThat(customerRepository.findAll().size()).isEqualTo(2);
    }
}
