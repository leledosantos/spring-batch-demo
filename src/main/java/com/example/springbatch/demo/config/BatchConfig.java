package com.example.springbatch.demo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.example.springbatch.demo.model.ZipFile;
import com.example.springbatch.demo.steps.tasklets.ZipFileDescompressTasklet;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	private static final String ZIPFILE_READER_NAME = "zipFileReaderName";	
	private static final String BASE_DIR = "#{jobParameters['baseDir']?: '/input/'}";
	private static final String FILE_NAME = "#{jobParameters['fileName']?: 'zip-file-example.zip'}";
	private static final int CHUNK_SIZE = 100; 
	
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	/*
	@Autowired 
	public TransactionRepository transactionRepository;
	*/
	
	@Autowired 
	public JobBuilderFactory jobBuilderFactory;
	
	@Bean
	@StepScope
	public FlatFileItemReader<ZipFile> reader(
			@Value(BASE_DIR) final String baseDir,
			@Value(FILE_NAME) final String fileName){
		
		return new FlatFileItemReaderBuilder<ZipFile>()
				.name(ZIPFILE_READER_NAME)
				.resource(new ClassPathResource(baseDir + fileName))
				//.lineMapper(new DefaultCompositeLineMapper())
				//.recordSeparatorPolicy(new DefaultRecordSeparatorPolicy())
				.linesToSkip(1)
				//.skippedLinesCallback(log::info)
				.build();
	}
	/*
	@Bean
	@StepScope
	public ZipFileItemProcessor processor(){
		return new ZipFileItemProcessor();
	}
	
	@Bean
	@StepScope
	public ZipFileItemWriter writer() {
		return new ZipFileItemWriter(transactionRepository);
	}*/
	
	@Bean
	public Step taskletStep() {
		return stepBuilderFactory
				.get("zipFileDescompressTasklet")
				.tasklet(new ZipFileDescompressTasklet(new FileSystemResource("C:/zip-file-example.zip"),"./target/zipfile/","zip-file-example.txt"))
				.build();
	}
	/*
	@Bean
	public Step chunckletStep() {
		return stepBuilderFactory.get("zipFileProcessingStep")
				.<ZipFile,ZipFile> chunk(CHUNK_SIZE)
				.reader(reader(null,null))
				.processor(processor())
				.writer(writer())
				.stream(reader(null,null))
				.build();
	}*/
	
	@Bean
	public Job zipFileJob() {
		return jobBuilderFactory.get("zipFileJob")
				.incrementer(new RunIdIncrementer())
				.start(taskletStep())
				//.next(chunckletStep())
				.build();
	}
	

}
