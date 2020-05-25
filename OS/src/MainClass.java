import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MainClass {

	public static void buildJobFile() {
////// يعبي الفايل
		Random r = new Random();

		int process = 150;
		String data = "";
		int totalSize = 0;

		for (int i = 0; i <process ; i++)

		{
			int cpu = r.nextInt(100 - 10) + 10;
			int size = r.nextInt(200 - 5) + 5;

			String pcb = "ProcesID:" + i + ";CPUburst:" + cpu + ";MBsize:" + size + "\n";
			data += pcb;

			totalSize += size;

		}
		System.out.println(data + "\n" + totalSize );
		File outFile = new File("jobData.txt");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(outFile));
			writer.write(data);
			writer.close();
		} catch (IOException ex) {
			System.out.println("Error writing file");
		}
	}

	public static void main(String[] args) {

		buildJobFile();

		Simulation s = new Simulation();

		s.fillRAM();
		int time = 3000;
		while (!s.finished()) {
			s.runCycle(time);
		}
		System.out.println(s.getResult() + "\n\nEND");

	}

}
