package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.cedarsoftware.util.io.JsonWriter;

public class JSONFormatting {

	private Path inputPath;
	private ArrayList<Path> filePathList;

	public void loadCorpus() throws Exception {
		this.filePathList = Files.walk(this.inputPath).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("done!");
		System.out.println("Number of file paths: " + filePathList.size());
	}

	private synchronized Path extractInputPath() {
		if (this.filePathList.isEmpty()) {
			return null;
		} else {
			return this.filePathList.remove(this.filePathList.size() - 1);
		}
	}

	public JSONFormatting setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public void run() throws Exception {

		while ((inputPath = JSONFormatting.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);
			

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);
			
			br.close();
			
			BufferedWriter bw = Files.newBufferedWriter(inputPath);
			bw.write(JsonWriter.formatJson(str.toString()));
			bw.close();

		}

	}

	public static void main(String[] ar) throws Exception {

		JSONFormatting jf = new JSONFormatting();
		jf.setInputPath(Paths.get("data/annotated/news_pilot/task4_output"));
		jf.loadCorpus();
		jf.run();

	}
}
