package com.example.springbatch.demo.steps.chuncklets;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.batch.item.ItemWriter;

import com.example.springbatch.demo.model.ZipFile;
import com.example.springbatch.demo.repositories.TransactionRepository;

public class ZipFileItemWriter implements ItemWriter<ZipFile>{
	private TransactionRepository transactionRepository;
	
	public ZipFileItemWriter(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Override
	@Transactional
	public void write(List items) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
