package ru.pets.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;

/**
 * @author Sergey_Komarov
 */
@SpringBootApplication
public class Main implements CommandLineRunner {
    @Autowired
    private Job job;
    @Autowired
    private JobLauncher jobLauncher;
    private Random rn = new Random();

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        JobParametersBuilder jobParameters = new JobParametersBuilder();
        int jobInstanceVersion= rn.nextInt(100000000);
        jobParameters.addString("trial", String.valueOf(jobInstanceVersion));
        jobLauncher.run(job, jobParameters.toJobParameters());
    }
}
