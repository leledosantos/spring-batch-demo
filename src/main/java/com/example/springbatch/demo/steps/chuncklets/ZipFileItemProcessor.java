package com.example.springbatch.demo.steps.chuncklets;

import org.springframework.batch.item.ItemProcessor;

import com.example.springbatch.demo.model.ZipFile;

public class ZipFileItemProcessor implements ItemProcessor<ZipFile, ZipFile>{
	@Override
	public ZipFile process(ZipFile item) throws Exception {
		// TODO Auto-generated method stub
		return item;
	}
}
