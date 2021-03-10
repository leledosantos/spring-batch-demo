package com.example.springbatch.demo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ZipUtil {
	
	private static final String EXTENSION_UPPERCASE = ".ZIP";
	private static final String EXTENSION_LOWERCASE = ".zip";
	
	public static File folder = new File("C:\\\\Users\\\\leandro.silva\\\\Desktop\\\\_LEANDRO\\\\shared");
	public static String temp = "";
	
	private static List<File> files = new ArrayList<File>();
	
	public static List<File> readAllofZipFilesFromSharedFolder(final File sharedFolder) {
		for(final File file : sharedFolder.listFiles()) {
			if(file.isDirectory()) {
				readAllofZipFilesFromSharedFolder(file);
			}else if(file.isFile()){
				String fileName = file.getName();
				if ((fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).toLowerCase()).equals("zip")) {
					files.add(new File(folder.getAbsolutePath()+ "\\" + file.getName()));
				}
			}
		}
		return files;
	}
	
	public static void unzipFolder(Path source, Path target) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = false;
                // example 1.1
                // some zip stored files and folders separately
                // e.g data/
                //     data/folder/
                //     data/folder/file.txt
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    // example 1.2
                    // some zip stored file path only, need create parent directories
                    // e.g data/folder/file.txt
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);

                    // copy files, classic
                    /*try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }*/
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();

        }

    }

    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
        throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
	
      
    public static void unzipAllOfFiles(File fileName, File outputFile) throws IOException{
    	String filName;
    	File folder = new File("C:\\Users\\leandro.silva\\Desktop\\_LEANDRO\\shared");
    	File[] listOfFiles = folder.listFiles(); 
    	for(int i = 0; i < listOfFiles.length; i++){
    		if (listOfFiles[i].isFile()) {
    			filName = listOfFiles[i].getName();
    			if (filName.endsWith(EXTENSION_LOWERCASE) || filName.endsWith(EXTENSION_UPPERCASE)) {
    				unZipFilesProcess(listOfFiles[i],outputFile);
                }
    		}
    	}
    }
	
	/**
	 * 
	 * */
	public static void zipFileProcess() throws IOException {

	}
	
	/*
	 * 
	 * **/
	public static void unZipFilesProcess(File zipFile, File outputFile) throws IOException {
        InputStream dataInputStream = new BufferedInputStream(new FileInputStream(zipFile));
        try {
            unZipFiles(dataInputStream, outputFile);
        } finally {
            IOUtils.closeQuietly(dataInputStream);
        }
    }
	
	/**
	 * 
	 * */
	public static void unZipFiles(InputStream datainputStream, File outputFolder) throws IOException {
        ZipInputStream dataZipInputStream = new ZipInputStream(datainputStream);
        ZipEntry zipEntry = dataZipInputStream.getNextEntry();

        while (zipEntry != null) {
            File file = new File(outputFolder, zipEntry.getName());
            OutputStream os = new BufferedOutputStream(FileUtils.openOutputStream(file));

            try {
                IOUtils.copy(dataZipInputStream, os);
            } finally {
                IOUtils.closeQuietly(os);
            }
            dataZipInputStream.closeEntry();
            zipEntry = dataZipInputStream.getNextEntry();
        }
    }
	
	public static void testerUnizip(File zipFile, File outputFile) throws IOException{
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.getAbsoluteFile()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
        	 File newFile = newFile(outputFile, zipEntry);
             if (zipEntry.isDirectory()) {
                 if (!newFile.isDirectory() && !newFile.mkdirs()) {
                     throw new IOException("Failed to create directory " + newFile);
                 }
             } else {
                 // fix for Windows-created archives
                 File parent = newFile.getParentFile();
                 if (!parent.isDirectory() && !parent.mkdirs()) {
                     throw new IOException("Failed to create directory " + parent);
                 }
                 
                 // write file content
                 FileOutputStream fos = new FileOutputStream(newFile);
                 int len;
                 while ((len = zis.read(buffer)) > 0) {
                     fos.write(buffer, 0, len);
                 }
                 fos.close();
             }
         zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
	}
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}

	/**
	 * 
	 * */
	public static void unzipZipsInDirTo(Path searchDir, Path unzipTo ){

	    final PathMatcher matcher = searchDir.getFileSystem().getPathMatcher("glob:**/*.zip");
	    try (final Stream<Path> stream = Files.list(searchDir)) {
	        stream.filter(matcher::matches)
	                .forEach(zipFile -> unzip(zipFile,unzipTo));
	    }catch (IOException e){
	        //handle your exception
	    }
	  }

	/**
	 * 
	 * */
	 public static void unzip(Path zipFile, Path outputPath){
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
	 
	 public static void match(String glob, String location) throws IOException {
			
			final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
			
			Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
				
				@Override
				public FileVisitResult visitFile(Path path,
						BasicFileAttributes attrs) throws IOException {
					if (pathMatcher.matches(path)) {
						System.out.println(path);
						unzip(path,Paths.get("C:\\Users\\leandro.silva\\Desktop\\_LEANDRO\\temp"));						
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc)
						throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
}
