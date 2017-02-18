package org.apache.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * General directory manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>comparing directories
 * </ul>
 * <p>
 * 
 * Original code: http://stackoverflow.com/questions/14522239/test-two-directory-trees-for-equality
 *
 */
public class DirectoryUtils {

    /**
     * checks if the directory file lists are equal.
     * 
     * If checkFileContent is true, then also if the file content is equal
     * 
     * @param directory
     *            the directory
     * @param compareDirectory
     *            the directory to compare with
     * @param checkFileContent
     *            also compare file content
     * @return true if directory and compareDirectory are equal
     * @throws IOException
     */
    public static boolean isEqual(Path directory, Path compareDirectory, boolean checkFileContent)
            throws IOException {

        boolean check = isEverythingInCompareDirectory(directory, compareDirectory, checkFileContent);

        // we only need to check file content in on direction.
        boolean checkOppositeFileContent = false;

        boolean checkOpposite = check
                && isEverythingInCompareDirectory(compareDirectory, directory, checkOppositeFileContent);
        return check && checkOpposite;

    }

    /**
     * checks if the directory file lists and file content is equal
     * 
     * @param directory
     *            the directory
     * @param compareDirectory
     *            the directory to compare with
     * @param checkFileContent
     *            also compare file content
     * @return true if directory and compareDirectory are equal
     * @throws IOException
     */
    private static boolean isEverythingInCompareDirectory(Path directory, Path compareDirectory,
            boolean checkFileContent) throws IOException {

        if (directory != null && compareDirectory != null) {
            // LOGGER.info("checking directory " + directory);
            // LOGGER.info("checking compareDirectory " + compareDirectory);

            File directoryFile = directory.toFile();
            File compareFile = compareDirectory.toFile();

            // check, if there is the same number of files/subdirectories
            File[] directoryFiles = directoryFile.listFiles();
            File[] compareFiles = compareFile.listFiles();

            if (directoryFiles != null && compareFiles != null) {
                //LOGGER.info("directoryFiles: " + directoryFiles.length + " vs compareFiles: " + compareFiles.length);
                if (directoryFiles.length == compareFiles.length) {
                    return compareDirectoryContents(directory, compareDirectory, checkFileContent);

                } else {
                    //LOGGER.info("number of files in directory are different " + directoryFiles.length + " vs compareDirectory: " + compareFiles.length);
                    return false;
                }
            } else {
                return checkForNulls(directoryFiles, compareFiles);
            }

        } else {
            // one of the directories is null
            return checkForNulls(directory, compareDirectory);
        }


    }

    private static boolean checkForNulls(Object directory, Object compareDirectory) {
        if (directory == null && compareDirectory == null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean compareDirectoryContents(Path directory, Path compareDirectory, boolean checkFileContent)
            throws IOException {

        // LOGGER.info("compareDirectoryContents: " + directory);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {

            for (Path directoryFilePath : directoryStream) {
                // LOGGER.info("processing " + directoryFilePath);
                // search for directoryFile in the compareDirectory
                Path compareFilePath = compareDirectory.resolve(directoryFilePath.getFileName());

                if (compareFilePath != null) {

                    File directoryFile = directoryFilePath.toFile();
                    if (directoryFile.isFile()) {
                        // LOGGER.info("checking file " + directoryFilePath);
                        if (checkFileContent && !FileUtils.contentEquals(compareFilePath.toFile(), directoryFile)) {
                            // LOGGER.info("files not equal: compare: " + compareFilePath.toFile() + ", directory: " +
                            // directoryFilePath.getFileName() + "!");
                            return false;
                        }

                    } else {
                        // LOGGER.info("going into recursion with directory " +directoryFilePath);
                        boolean result = isEverythingInCompareDirectory(directoryFilePath, compareFilePath,
                                checkFileContent);

                        // cancel if not equal, otherwise continue processing
                        if (!result) {
                            return false;
                        }
                    }
                } else {
                    // LOGGER.info(directoryFilePath.toString() + ":compareFilepath not found");
                    return false;
                }


            }
        }

        return true;
    }
}
