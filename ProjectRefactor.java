import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Project refactoring: Copy files, replacing both filenames and file contents
 */
public class ProjectRefactor {
	public static void main(String[] args) throws IOException {
		run(new String[]{
				"/Users/wangzhenyang/Development Projects/YituProjects/yitu-road"
				, "/Users/wangzhenyang/Development Projects/YituProjects/yitu-road-2"
				, "ruoyi", "yitu"
				, "Ruoyi", "Yitu"
				, "RUOYI", "YITU"
		});
	}
	
	private static void run(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: <sourceDirectory> <targetDirectory> [replacements...]");
			return;
		}
		
		// 解析命令行参数
		String sourceDir = args[0];
		String targetDir = args[1];
		Map<String, String> replacements = new HashMap<>();
		
		for (int i = 2; i < args.length; i += 2) {
			if (i + 1 < args.length) {
				replacements.put(args[i], args[i + 1]);
			}
		}
		
		// Copy files with renaming and content replacement.
		copyAndRenameFiles(Paths.get(sourceDir), Paths.get(targetDir), replacements);
	}
	
	private static void copyAndRenameFiles(Path sourceDir, Path targetDir, Map<String, String> replacements) throws IOException {
		if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
			System.out.println("Source directory does not exist or is not a directory.");
			return;
		}
		
		Files.walk(sourceDir).forEach(sourcePath -> {
			try {
				Path relativePath = sourceDir.relativize(sourcePath);
				Path targetPath = targetDir.resolve(relativePath);
				
				// Handle substitutions in filenames.
				String newFileName = replaceFileName(relativePath.getFileName().toString(), replacements);
				if (!newFileName.equals(relativePath.getFileName().toString())) {
					System.out.println("File renamed: " + relativePath + " -> " + newFileName);
				}
				
				targetPath = targetPath.getParent().resolve(newFileName);
				
				// Creating the target directory.
				if (Files.isDirectory(sourcePath)) {
					Files.createDirectories(targetPath);
				} else {
					Files.createDirectories(targetPath.getParent());
					
					// Read the content of the source file and replace it.
					String content = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
					content = replaceContent(content, replacements);
					
					// The modified content is written to the target file.
					try (BufferedWriter writer = Files.newBufferedWriter(targetPath, StandardCharsets.UTF_8)) {
						writer.write(content);
					}
				}
			} catch (IOException e) {
				System.err.println("Error copying file: " + sourcePath + " - " + e.getMessage());
			}
		});
	}
	
	private static String replaceFileName(String fileName, Map<String, String> replacements) {
		String newFileName = fileName;
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			newFileName = newFileName.replace(entry.getKey(), entry.getValue());
		}
		return newFileName;
	}
	
	private static String replaceContent(String content, Map<String, String> replacements) {
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			content = content.replace(entry.getKey(), entry.getValue());
		}
		return content;
	}
}
