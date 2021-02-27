package com.example.springbatch.demo.steps.tasklets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

//implements Tasklet Interface
public class ZipFileDescompressTasklet implements Tasklet{
	
	//Declares Tasklet parameters
	private Resource resource;
	private String directory;
	private String file;
	
	public ZipFileDescompressTasklet(Resource resource, String directory, String file) {
		this.resource = resource;
		this.directory = directory;
		this.file = file;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		//Opes archive
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(resource.getInputStream()));
		
		//Creates target directory if absent
		File targetDirectoryAsFile = new File(directory);
		if(!targetDirectoryAsFile.exists()) {
			FileUtils.forceMkdir(targetDirectoryAsFile);
		}
		
		//Descompress arquive
		File target = new File(directory,file);
		BufferedOutputStream destiny = null;
		while(zis.getNextEntry() != null) {
			if(!target.exists()) {
				target.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(target);
			destiny = new BufferedOutputStream(fos);
			IOUtils.copy(zis,destiny);
			destiny.flush();
			destiny.close();
		}
		zis.close();
		if(!target.exists()) {
			throw new IllegalStateException("Could not descompress anything from the archive!");
		}
		//tasklet finishes
		return RepeatStatus.FINISHED;
	}
}
