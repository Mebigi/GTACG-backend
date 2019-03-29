package Wrapper;

import java.io.IOException;

public class R {
	public static String mast(String tree1, String tree2, boolean root) throws IOException, InterruptedException {
		String command = "Rscript --vanilla scripts/mast.r '" + tree1 + "' '" + tree2 + "' " + root;
		//System.out.println(command);
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();
		
		try {
			byte array[] = new byte[process.getInputStream().available()];
			process.getInputStream().read(array);
			String result = new String(array);
			return result;
		} catch (Exception e) {
			System.out.println(command);
		}
		return "";
	}
	
	public static double treeDist(String tree1, String tree2, boolean root) throws IOException, InterruptedException {
		String command = "Rscript --vanilla scripts/treeDist.r '" + tree1 + "' '" + tree2 + "' " + root;
		//System.out.println(command);
		Process process = new ProcessBuilder(new String[] {"bash", "-c", command}).redirectErrorStream(true).start();
		process.waitFor();
		
		try {			
			byte array[] = new byte[process.getInputStream().available()];
			process.getInputStream().read(array);
			String result = new String(array);
			return Double.parseDouble(result);
		} catch (Exception e) {
			System.out.println(command);
		}
		return 0;
	}

	
}
