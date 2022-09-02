package ru.pets.batch.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.pets.batch.Pet;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Sergey_Komarov
 */
@Slf4j
@EnableBatchProcessing
@Configuration
public class BatchConfig {
    @Autowired
    private JobBuilderFactory jobs;
    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    @Qualifier(value = "postgresDS")
    private DataSource dataSource;
    private Random rn = new Random();

    @Bean
    public Job job(Step step, BatchListener batchListener) {
        return jobs.get("job")
                .listener(batchListener)
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step step(ItemReader<Pet> itemReader, ItemWriter<Pet> itemWriter) {
        return steps
                .get("step1")
                .<Pet, Pet>chunk(10)
                .reader(itemReader)
                .processor(itemProcessor())
                .writer(itemWriter)
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Pet> itemReader() {
        JdbcPagingItemReader<Pet> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);

        reader.setFetchSize(20);
        reader.setRowMapper(new PetRowMapper());
        reader.setPageSize(1);

        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id, name, age, weight");
        mySqlPagingQueryProvider.setFromClause("from pets");
        mySqlPagingQueryProvider.setWhereClause("age = 0");
        Map<String, Order> orderByName = new HashMap<>();
        orderByName.put("id", Order.ASCENDING);
        mySqlPagingQueryProvider.setSortKeys(orderByName);

        reader.setQueryProvider(mySqlPagingQueryProvider);

        return reader;
    }

    @Bean
    ItemProcessor<Pet, Pet> itemProcessor() {
        return pet -> {
            log.info("Processing start" + pet.getName() + ", id=" + pet.getId());

            if (pet.getWeight() == 0.0) {
                throw new RuntimeException("Some error");
            }

            pet.setAge(rn.nextInt(10) + 1);
            log.info("Processing end" + pet.getName() + ", id=" + pet.getId());

            return pet;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Pet> itemWriter() {
        JdbcBatchItemWriter<Pet> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("update pets set age = :age where id = :id");

        return writer;
    }

    @Bean(name = "postgresDS")
    public DataSource postgresBatchDataSource(@Value("${spring.datasource.url}") String url,
                                              @Value("${spring.datasource.driver-class-name}") String driverClassName,
                                              @Value("${spring.datasource.username}") String userName,
                                              @Value("${spring.datasource.password}") String password) {
        return DataSourceBuilder.create()
                .url(url)
                .driverClassName(driverClassName)
                .username(userName)
                .password(password).build();
    }
}
