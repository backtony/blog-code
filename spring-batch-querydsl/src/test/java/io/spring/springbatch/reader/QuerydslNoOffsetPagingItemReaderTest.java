package io.spring.springbatch.reader;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.spring.springbatch.TestBatchConfig;
import io.spring.springbatch.customer.Customer;
import io.spring.springbatch.customer.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import org.springframework.batch.item.querydsl.reader.expression.Expression;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetNumberOptions;
import org.springframework.batch.item.querydsl.reader.options.QuerydslNoOffsetStringOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManagerFactory;

import static io.spring.springbatch.customer.QCustomer.customer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TestBatchConfig.class})
class QuerydslNoOffsetPagingItemReaderTest {

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EntityManagerFactory emf;

    @AfterEach
    void tearDown(){
        customerRepository.deleteAllInBatch();
    }

    @Test
    public void 쿼리생성후_체이닝여부_확인() {
        //given

        JPAQuery<Customer> query = queryFactory
                .selectFrom(customer)
                .where(customer.age.between(25, 26))
                .orderBy(customer.age.asc());

        NumberPath<Long> id = customer.id;
        BooleanExpression where = id.gt(1);
        OrderSpecifier<Long> order = id.asc();

        //when
        query.where(where).orderBy(order);

        //then
        assertThat(query.toString()).contains("customer.id >");
        assertThat(query.toString()).contains("customer.id asc");
    }

    @Test
    public void 쿼리생성후_select_오버라이딩_확인() {
        //given
        JPAQuery<Customer> query = queryFactory
                .selectFrom(customer)
                .where(customer.age.between(25, 26))
                .orderBy(customer.age.asc());

        NumberPath<Long> id = customer.id;

        //when
        query.select(id.max().add(1));
        System.out.println(query);

        //then
        assertThat(query.toString()).contains("select max(customer.id)");
    }

    @Test
    public void reader가_정상적으로_값을반환한다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        QuerydslNoOffsetNumberOptions<Customer, Long> options = new QuerydslNoOffsetNumberOptions<>(customer.id, Expression.ASC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                        .selectFrom(customer)
                        .where(customer.age.goe(26)));

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
    public void reader가_역순으로_값을반환한다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        QuerydslNoOffsetNumberOptions<Customer, Long> options = new QuerydslNoOffsetNumberOptions<>(customer.id, Expression.DESC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.goe(26)));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();

        //then
        assertThat(read1.getAge()).isEqualTo(27);
        assertThat(read2.getAge()).isEqualTo(26);
        assertThat(read3).isNull();
    }

    @Test
    public void 빈값일경우_null이_반환된다() throws Exception {
        //given

        QuerydslNoOffsetNumberOptions<Customer, Long> options = new QuerydslNoOffsetNumberOptions<>(customer.id, Expression.ASC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.eq(25)));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();

        //then
        assertThat(read1).isNull();
    }

    @Test
    public void pageSize에_맞게_값을반환한다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));
        customerRepository.save(new Customer(name,28));

        QuerydslNoOffsetNumberOptions<Customer, Long> options = new QuerydslNoOffsetNumberOptions<>(customer.id, Expression.ASC);

        int chunkSize = 2;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.goe(25)));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();
        Customer read4 = reader.read();

        //then
        assertThat(read1.getAge()).isEqualTo(26);
        assertThat(read2.getAge()).isEqualTo(27);
        assertThat(read3.getAge()).isEqualTo(28);
        assertThat(read4).isNull();
    }

    @Test
    public void int필드도_nooffset이_적용된다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name,26));
        customerRepository.save(new Customer(name,27));

        QuerydslNoOffsetNumberOptions<Customer, Integer> options = new QuerydslNoOffsetNumberOptions<>(customer.age, Expression.DESC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.name.eq(name)));

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();
        Customer read2 = reader.read();
        Customer read3 = reader.read();

        //then
        assertThat(read1.getAge()).isEqualTo(27);
        assertThat(read2.getAge()).isEqualTo(26);
        assertThat(read3).isNull();
    }

    @Test
    public void 조회결과가없어도_정상조회된다() throws Exception {
        //given

        QuerydslNoOffsetNumberOptions<Customer, Integer> options = new QuerydslNoOffsetNumberOptions<>(customer.age, Expression.DESC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer))
                ;

        reader.open(new ExecutionContext());

        //when
        Customer read1 = reader.read();

        //then
        assertThat(read1).isNull();
    }

    @Test
    public void 문자열필드_DESC_nooffset이_적용된다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name+1,26));
        customerRepository.save(new Customer(name+2,26));

        QuerydslNoOffsetStringOptions<Customer> options = new QuerydslNoOffsetStringOptions<>(customer.name, Expression.DESC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.eq(26)));

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

    @Test
    public void 문자열필드_ASC_nooffset이_적용된다() throws Exception {
        //given
        String name = "backtony";
        customerRepository.save(new Customer(name+1,26));
        customerRepository.save(new Customer(name+2,26));

        QuerydslNoOffsetStringOptions<Customer> options = new QuerydslNoOffsetStringOptions<>(customer.name, Expression.ASC);

        int chunkSize = 1;

        QuerydslNoOffsetPagingItemReader<Customer> reader = new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(customer)
                .where(customer.age.eq(26)));

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
}
