package com.tcoded;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SimpleUnzipper {

    public static void main(String[] args) {
        System.out.println("-- Simpler Unzipper --");
        String zipFilePathName = args.length > 0 ? args[0] : null;
        String destDirectoryName = args.length > 1 ? args[1] : null;
        String verboseArg = args.length > 2 ? args[2] : "true";

        // Parse input file
        File zipFile;
        File wd = new File(System.getProperty("user.dir"));
        if (zipFilePathName == null) {
            // Find first zip file in the dir and use that
            System.out.println("No zip file was specified, using the first one we find...");
            zipFile = findFirstZipFileInDir(wd);
        } else {
            zipFile = new File(wd, zipFilePathName);
        }

        // Parse output folder
        File destDir;
        if (destDirectoryName == null) {
            destDir = wd;
        } else {
            destDir = new File(wd, destDirectoryName);
        }

        // Parse verbose option
        boolean verbose = verboseArg.equals("true");

        // Sanity checks
        String effectiveZipFileAbsPath = zipFile == null ? "NULL" : zipFile.getAbsolutePath();
        String effectiveDestDirAbsPath = destDir.getAbsolutePath();
        if (zipFile == null || !zipFile.exists()) {
            System.out.printf("Could not find the zip file specified (%s)%n", effectiveZipFileAbsPath);
            return;
        }
        if (!destDir.exists()) {
            System.out.printf("Could not find the destination folder specified (%s)%n", effectiveDestDirAbsPath);
            return;
        }
        if (!destDir.isDirectory()) {
            System.out.printf("The destination specified is not a folder (%s)%n", effectiveDestDirAbsPath);
            return;
        }

        System.out.printf("Using source file: %s%n", effectiveZipFileAbsPath);
        System.out.printf("Using destination folder: %s%n", destDir.getAbsolutePath());

        try {
            System.out.println();
            System.out.println("Unzipping file...");

            unzip(zipFile, destDir, verbose);

            System.out.println();
            System.out.println("-----");
            System.out.println("DONE!");
            System.out.println("-----");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void unzip(File zipFile, File destDir, boolean verbose) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

            ZipEntry currentEntry;
            while ((currentEntry = zis.getNextEntry()) != null) {
                // Verbose
                if (verbose) System.out.printf(" - Unpacking %s", currentEntry.getName());

                // Filter hidden folders
                // noinspection SpellCheckingInspection
                if (currentEntry.getName().startsWith("__MACOSX/")) {
                    if (verbose) System.out.println("   (Skipping...)");
                    continue;
                } else {
                    if (verbose) System.out.println();
                }

                // Safely unpack current entry to destination file
                File currentFile = safeNewFile(destDir, currentEntry);

                // Is entry a folder
                if (currentEntry.isDirectory()) {
                    if (currentFile.exists() && !currentFile.isDirectory())
                        throw new IOException("File with the name \"%s\" already exists but isn't a folder!".formatted(currentEntry.getName()));
                    else if (!currentFile.exists() && !currentFile.mkdirs()) {
                        throw new IOException("Failed to create folder with the name \"%s\"!".formatted(currentEntry.getName()));
                    }
                }

                // Is entry a file
                else {
                    // Ensure that the folder in which to place the file exists
                    File parentFolder = currentFile.getParentFile();

                    if (!parentFolder.isDirectory() && !parentFolder.mkdirs()) {
                        throw new IOException("Failed to create folder with the name \"%s\"!".formatted(currentEntry.getName()));
                    }

                    // Write the file
                    try (FileOutputStream fileOutputStream = new FileOutputStream(currentFile)) {
                        int bufferLenth;
                        while ((bufferLenth = zis.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, bufferLenth);
                        }
                    } // auto-close fileOutputStream
                }
            }

            zis.closeEntry();
        } // auto-close zis
    }

    private static File findFirstZipFileInDir(File wd) {
        File[] files = wd.listFiles((a, b) -> b.endsWith(".zip"));
        if (files == null) files = new File[] {};
        for (File file : files) {
            return file;
        }
        return null;
    }

    public static File safeNewFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}