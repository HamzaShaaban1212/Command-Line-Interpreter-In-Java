import java.io.*;
import java.util.Scanner;

public class CommandLineInterpreter {
    private static String currentDirectory = System.getProperty("user.dir");

    public static void main(String[] args) {
        System.out.println("Welcome to the Java Unix-based shell...");

        boolean isExit = false;
        Scanner scanner = new Scanner(System.in);

        while (!isExit) {
            System.out.print("$> ");
            String userInput = scanner.nextLine().trim();

            // Check for pipe operator
            if (userInput.contains("|")) {
                String[] commands = userInput.split("\\|");
                String previousOutput = "";
                for (String command : commands) {
                    command = command.trim();
                    previousOutput = processCommand(command, previousOutput);
                }
                // Output the result of the last command in the pipeline
                if (!previousOutput.isEmpty()) {
                    System.out.println(previousOutput);
                }
                continue;
            } else if (userInput.contains(">>")) {
                String[] redirectionParts = userInput.split(">>");
                String command = redirectionParts[0].trim();
                String targetFile = redirectionParts[1].trim();
                String output = processCommand(command, "");
                appendToFile(targetFile, output);
                continue;
            } else if (userInput.contains(">")) {
                String[] redirectionParts = userInput.split(">");
                String command = redirectionParts[0].trim();
                String targetFile = redirectionParts[1].trim();
                String output = processCommand(command, "");
                writeToFile(targetFile, output);
                continue;
            }

            String output = processCommand(userInput, "");
            if (!output.isEmpty()) {
                System.out.println(output);
            }
        }

        scanner.close();
    }

    private static String processCommand(String userInput, String previousOutput) {
        // Check for commands that need to use the previous output as input
        String[] inputParts = userInput.split("\\s+", 2);
        String command = inputParts[0];
        String parameters = inputParts.length > 1 ? inputParts[1] : "";

        // If there's a previous output, treat it as input for this command
        if (!previousOutput.isEmpty() && !command.equals("cat")) {
            parameters = previousOutput;
        }

        switch (command) {

            case "help":
                return displayHelp();

            case "pwd":
                return printWorkingDirectory();

            case "mkdir":
                return makeDirectory(parameters);

            case "rmdir":
                return removeDirectory(parameters);

            case "touch":
                return createFile(parameters);

            case "mv":
                String[] mvParams = parameters.split("\\s+");
                if (mvParams.length == 2) {
                    moveFile(mvParams[0], mvParams[1]);
                } else {
                    return "$[error]> Please provide source and destination.";
                }
                return "";


            case "rm":
                deleteFile(parameters);
                return "";

            case "cat":
                return displayFileContent(parameters);


            case "exit":
                System.out.println("$[See you next time ;D]> Bye!");
                System.exit(0);
                return "";
            default:
                return "$[error]> Unknown command: " + command;
        }
    }

    private static String displayHelp() {
        return "Available commands: \n"
                + " ls [-a | -r]: list current directory child items \n"
                + " cd <directory>: change directory \n"
                + " pwd : print working directory \n"
                + " mkdir <directory>: make directory \n"
                + " rmdir <directory>: remove empty directory \n"
                + " touch <file>: create new file \n"
                + " mv <source> <destination>: cut/rename a file \n"
                + " rm <file>: remove a file \n"
                + " cat <file>: output file's content \n"
                + " ============================= \n"
                + " Optional directives: \n"
                + " > [overwrite] \n"
                + " >> [append new line] \n"
                + " | [pipe output]";
    }

    private static String printWorkingDirectory() {
        return currentDirectory;
    }

    private static String makeDirectory(String parameters) {
        if (parameters.isEmpty()) {
            return "$[error]> Please specify a directory name.";
        }
        File newDir = new File(currentDirectory, parameters);
        if (newDir.mkdir()) {
            return "Directory created: " + parameters;
        } else {
            return "$[error]> Directory already exists or cannot be created.";
        }
    }

    private static String removeDirectory(String parameters) {
        if (parameters.isEmpty()) {
            return "$[error]> Please specify a directory name.";
        }
        File dir = new File(currentDirectory, parameters);
        if (dir.exists() && dir.isDirectory()) {
            if (dir.delete()) {
                return "Directory removed: " + parameters;
            } else {
                return "$[error]> Directory could not be removed.";
            }
        } else {
            return "$[error]> No such directory: " + parameters;
        }
    }

    private static String createFile(String parameters) {
        if (parameters.isEmpty()) {
            return "$[error]> Please specify a file name.";
        }
        File file = new File(currentDirectory, parameters);
        try {
            if (file.createNewFile()) {
                return "File created: " + parameters;
            } else {
                return "$[error]> File already exists.";
            }
        } catch (IOException e) {
            return "$[error]> An error occurred while creating the file.";
        }
    }

    private static void moveFile(String source, String destination) {
        File srcFile = new File(currentDirectory, source);
        File destFile = new File(currentDirectory, destination);

        // Check if the source file exists
        if (!srcFile.exists()) {
            System.out.println("$[error]> Source file does not exist: " + source);
            return;
        }

        // Check if the destination is a directory or file
        if (destFile.isDirectory()) {
            // If the destination is a directory, append the source file name to it
            destFile = new File(destFile, srcFile.getName());
        }

        // Attempt to move the file
        try {
            if (srcFile.renameTo(destFile)) {
                System.out.println("File moved from " + source + " to " + destination);
            } else {
                System.out.println("$[error]> Failed to move file: " + source);
            }
        } catch (Exception e) {
            System.out.println("$[error]> Error occurred while moving file: " + e.getMessage());
        }
    }

    private static void deleteFile(String fileName) {
        File file = new File(currentDirectory, fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File removed: " + fileName);
            } else {
                System.out.println("$[error]> Failed to remove file: " + fileName);
            }
        } else {
            System.out.println("$[error]> File does not exist: " + fileName);
        }
    }

    private static String displayFileContent(String fileName) {
        File file = new File(currentDirectory, fileName);
        StringBuilder content = new StringBuilder();
        if (file.exists() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                return "$[error]> " + e.getMessage();
            }
        } else {
            return "$[error]> File does not exist: " + fileName;
        }
        return content.toString().trim();
    }

    private static void writeToFile(String targetFile, String content) {
        try (FileWriter fw = new FileWriter(new File(currentDirectory, targetFile))) {
            fw.write(content);
        } catch (IOException e) {
            System.out.println("$[error]> " + e.getMessage());
        }
    }

    private static void appendToFile(String targetFile, String content) {
        try (FileWriter fw = new FileWriter(new File(currentDirectory, targetFile), true)) {
            fw.write(content);
        } catch (IOException e) {
            System.out.println("$[error]> " + e.getMessage());
        }
    }
}