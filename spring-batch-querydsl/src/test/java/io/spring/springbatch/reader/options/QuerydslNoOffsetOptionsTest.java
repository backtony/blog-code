package io.spring.springbatch.reader.options;


import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.spring.springbatch.TestBatchConfig;
import io.spring.springbatch.customer.Customer;
import io.spring.springbatch.customer.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.querydsl.reader.expression.Expression;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetNumberOptions;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetStringOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

import static io.spring.springbatch.customer.QCustomer.customer;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {TestBatchConfig.class})
public class QuerydslNoOffsetOptionsTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    public void after() throws Exception {
        customerRepository.deleteAll();
    }

    @Test
    public void path변수에서_필드명을_추출한다() throws Exception {
        //given
        String expected = "id";
        NumberPath<Long> path = customer.id;

        //when
        QuerydslNoOffsetNumberOptions<Customer, Long> options = new QuerydslNoOffsetNumberOptions<>(path,  Expression.ASC);

        //then
        assertThat(options.getFieldName()).isEqualTo(expected);
    }

    @Test
    public void Number_firstId_lastId_저장된다() {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        QuerydslNoOffsetNumberOptions<Customer, Long> options =
                new QuerydslNoOffsetNumberOptions<>(customer.id, Expression.ASC);

        Function<JPAQueryFactory, JPAQuery<Customer>> query = factory -> factory
                .selectFrom(customer)
                .where(customer.name.startsWith(name));
        JPAQuery<Customer> apply = query.apply(queryFactory);

        // when
        options.initKeys(apply, 0);

        // then
        assertThat(options.getCurrentId() <options.getLastId()).isTrue();
    }

    @Test
    public void String_firstId_lastId_저장된다() {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name+1,26));
        customerRepository.save(new Customer(name+2,27));

        QuerydslNoOffsetStringOptions<Customer> options =
                new QuerydslNoOffsetStringOptions<>(customer.name, Expression.DESC);

        Function<JPAQueryFactory, JPAQuery<Customer>> query = factory -> factory
                .selectFrom(customer)
                .where(customer.name.startsWith(name));
        JPAQuery<Customer> apply = query.apply(queryFactory);

        // when
        options.initKeys(apply, 0);

        // then
        assertThat(options.getCurrentId()).isEqualTo(name+2);
        assertThat(options.getLastId()).isEqualTo(name+1);
    }

    @Test
    public void groupBy절인지_확인_할수_있다() throws Exception {
        //given
        QuerydslNoOffsetStringOptions<Customer> options =
                new QuerydslNoOffsetStringOptions<>(customer.name, Expression.DESC);

        Function<JPAQueryFactory, JPAQuery<Customer>> query = factory -> factory
                .selectFrom(customer)
                .where(customer.name.startsWith("backtony"))
                .groupBy(customer.name);

        JPAQuery<Customer> apply = query.apply(queryFactory);

        //when
        boolean isGroupBy = options.isGroupByQuery(apply);

        //then
        assertThat(isGroupBy).isTrue();
    }

    @Test
    public void group만_있으면_false() throws Exception {
        //given
        QuerydslNoOffsetStringOptions<Customer> options =
                new QuerydslNoOffsetStringOptions<>(customer.name, Expression.DESC);

        //when
        boolean isGroupBy = options.isGroupByQuery("select group from class");

        //then
        assertThat(isGroupBy).isFalse();
    }
}
