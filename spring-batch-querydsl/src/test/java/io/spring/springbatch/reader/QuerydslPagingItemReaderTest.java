package io.spring.springbatch.reader;

import io.spring.springbatch.TestBatchConfig;
import io.spring.springbatch.customer.Customer;
import io.spring.springbatch.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.querydsl.reader.QuerydslPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManagerFactory;

import static io.spring.springbatch.customer.QCustomer.customer;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {TestBatchConfig.class})
class QuerydslPagingItemReaderTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManagerFactory emf;

    @BeforeEach
    void tearDown(){
        customerRepository.deleteAllInBatch();
    }

    @Test
    void reader가_정상적으로_값을_반환한다() throws Exception{
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        int pageSize=1;

        QuerydslPagingItemReader<Customer> reader = new QuerydslPagingItemReader<>(emf, pageSize, query -> query
                .selectFrom(customer)
                .where(customer.name.startsWith(name)));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();

        //then
        assertThat(read1.getAge()).isEqualTo(26);
        assertThat(read2.getAge()).isEqualTo(27);
        assertThat(read3).isNull();
    }

    @Test
    public void 빈값일경우_null이_반환된다() throws Exception {
        //given

        int pageSize=1;

        QuerydslPagingItemReader<Customer> reader = new QuerydslPagingItemReader<>(emf, pageSize, query -> query
                .selectFrom(customer)
                .where(customer.name.startsWith("hello")));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();

        //then
        assertThat(read1).isNull();
    }


}