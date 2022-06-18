package io.spring.springbatch.reader;


import io.spring.springbatch.TestBatchConfig;
import io.spring.springbatch.customer.Customer;
import io.spring.springbatch.customer.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import org.springframework.batch.item.querydsl.reader.expression.Expression;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetStringOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManagerFactory;

import static io.spring.springbatch.customer.QCustomer.customer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestBatchConfig.class})
class QuerydslNoOffsetPagingItemReaderGroupByTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManagerFactory emf;

    @AfterEach
    public void after() throws Exception {
        customerRepository.deleteAllInBatch();
    }

    @Test
    void groupBy_ASC_nooffset이_적용된다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name+1,26));
        customerRepository.save(new Customer(name+2,27));

        QuerydslNoOffsetStringOptions<Customer> options = new QuerydslNoOffsetStringOptions<>(customer.name, Expression.ASC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.goe(26))
                .groupBy(customer.name)
        );

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();

        //then
        assertThat(read1.getName()).isEqualTo(name+1);
        assertThat(read2.getName()).isEqualTo(name+2);
        assertThat(read3).isNull();
    }

    @Test
    public void groupBy_DESC_nooffset이_적용된다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name+1,26));
        customerRepository.save(new Customer(name+2,27));

        QuerydslNoOffsetStringOptions<Customer> options = new QuerydslNoOffsetStringOptions<>(customer.name, Expression.DESC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.goe(26))
                .groupBy(customer.name)
        );

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();


        //then
        assertThat(read1.getName()).isEqualTo(name+2);
        assertThat(read2.getName()).isEqualTo(name+1);
        assertThat(read3).isNull();
    }
}
