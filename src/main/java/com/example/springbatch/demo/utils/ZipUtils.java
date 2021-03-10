package com.example.springbatch.demo.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.batch.core.UnexpectedJobExecutionException;

public class ZipUtils {
	
	private static final String JOKER = "glob:**/*.zip";
	
	/**
	 * Metodo responsavel em fazer a leitura da pata compartilhada e retornar todos
	 * os paths dos arquivos zip encontrados
	 * 
	 * @param Path sharedFolder
	 * @return List<Path> paths
	 * 
	 * */
	public static List<Path> readZipFilesIn(Path sharedFolder) throws IOException {
		System.out.println("Reading files in: " + sharedFolder + "...");
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(JOKER);
		List<Path> paths = new ArrayList<Path>();
		//
		Files.walkFileTree(sharedFolder, new SimpleFileVisitor<Path>() {		
			@Override
			public FileVisitResult visitFile(Path path,	BasicFileAttributes attrs) throws IOException {
				if (pathMatcher.matches(path)) {
					System.out.println(path);
					//
					paths.add(path);			
				}
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return paths;
	}
	
	/**
	 * Metodo responsavel em descompactar os arquivos zips
	 * e copia-los para o diretorio de saida
	 * 
	 * @param Path zipFile
	 * @param Path sharedFolder
	 * 
	 * */
	public static void unzipFilesToTempFolder(Path zipFile, Path outputPath){
		System.out.println("Unzip files: " + zipFile + " to: " + outputPath + "...");
	    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
	        ZipEntry entry = zis.getNextEntry();
	        while (entry != null) {
	            Path newFilePath = outputPath.resolve(entry.getName());
	            if (entry.isDirectory()) {
	                Files.createDirectories(newFilePath);
	            } else {
	                if(!Files.exists(newFilePath.getParent())) {
	                    Files.createDirectories(newFilePath.getParent());
	                }
	                try (OutputStream bos = Files.newOutputStream(outputPath.resolve(newFilePath))) {
	                    byte[] buffer = new byte[Math.toIntExact(entry.getSize())];

	                    int location;

	                    while ((location = zis.read(buffer)) != -1) {
	                        bos.write(buffer, 0, location);
	                    }
	                }
	            }
	            entry = zis.getNextEntry();
	        }
	    }catch(IOException e){
	        throw new RuntimeException(e);
	        //handle your exception
	    }
	  }
	
	/**
	 * Metodo responsavel em compiar os arquivos zips
	 * para uma pasta de backup
	 * 
	 * @param Path path
	 * @param Path sharedFolder
	 * 
	 * */
	public static void copyZipFilesToBackupFolder(Path path, Path backupFolder) throws IOException {
		System.out.println("Moving files to..." + backupFolder.toFile().getAbsolutePath());
		if(!backupFolder.toFile().exists()) {
			Files.createDirectories(backupFolder);
		}
		String fileName = path.toFile().getAbsolutePath().substring(path.toFile().getAbsolutePath().lastIndexOf("\\")+1);
		Files.copy(path, backupFolder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Metodo responsavel em limpar os arquivos
	 *  
	 * @param Path path
	 * @param Path sharedFolder
	 * 
	 * */
	public static void deleteZipFilesInSharedFolder(Path path) throws IOException {
			//File file = path.toFile();
			System.out.println("Deleting files..." + path.toFile().getAbsolutePath());
			boolean deleted = Files.deleteIfExists(path);
			if (!deleted) {
                throw new UnexpectedJobExecutionException("Could not delete file " + path.toFile().getPath());
            }
	}			

}
