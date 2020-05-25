import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Simulation {

	PCB IODevice;

	PCB runningProcess;

	Queue ready, job, IOQueue, AdditionalMemory;

	int internalClock;
	int ioTimer;
	int cputime;

	int normCnt;
	int abnormCnt;

	int CPUcnt;///// cpu bound or io bound
	int IOFREE;/////
	int Addsize;//// process need extra space
	long lts;///////

	String msgs;
	String result;
	String fileResult;

	public Simulation() {
		job = new Queue();////
		ready = new Queue(598*1024);
		AdditionalMemory = new Queue(106*1024); 
		result = "";
		IOQueue = new Queue();
		loadJobs();
		fileResult = "ID" + "\t" + "AR" + "\t\t" + "NOC" + "       " + "TSICPU" + "\t" + "NIO" + "\t\t" + "PIO"
				+ "\t\t" + "NOWM" + "\t" + "NOP" + "\t\t" + "TTOK " + "\t" + "FST" + "\n";
		IODevice = null;
		//result1="";
		internalClock = 1000;
		runningProcess = null;
		msgs = "";
		IOFREE = 0;
		Addsize = 0;
		ioTimer = 0;
		normCnt = 0;
		abnormCnt = 0;
		cputime = 0;
		CPUcnt = 0;
		lts = 0;
	}

	public String getResult() {
		return msgs + "\n\n" + result;
	}

	private void loadJobs() {////// يفضي الفايل
		Scanner input;
		int numberOfprocess = 0;
		int totalSize = 0;
		try {
			input = new Scanner(new File("jobData.txt"));
			while (input.hasNext()) {
				numberOfprocess++;
				String line = input.nextLine();
				String[] items = line.split(";");

				String[] data = items[0].split(":");
				int id = Integer.parseInt(data[1]);

				data = items[1].split(":");
				int cpu = Integer.parseInt(data[1]);

				data = items[2].split(":");
				int sz = Integer.parseInt(data[1]);

				totalSize += sz;

				PCB pcb = new PCB(id, cpu, sz, " ");
				if (job.acceptReqSize(sz)) {
					job.insertByBurst(pcb);

				} else
					System.out.println("Error adding to job" + job.sizeOfQueue + ":" + sz);

				
			}
			result += "1. The number of initially generated jobs stored on the H-disk are:" + numberOfprocess + " job\n";
			result += "2. The average program size of all jobs is:" + ((double) totalSize) / numberOfprocess + "MB\n";
			
//			PCB processOne = new PCB(99,8,1999);
//			if (job.acceptReqSize(processOne.size))
//				job.insertBySize(processOne);
			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error file nottt found");
		}

		
	}


	public void fillRAM() {
		if (!job.isEmpty()) {
			PCB t = job.peek();
			System.out.println("before:"+t);
			while (!job.isEmpty() && ready.acceptReqSize(t.getSize())) {
				t.status = "READY";
				System.out.println("inside"+t);

				t.loadedInReadyQueue = internalClock++;
				ready.insertByBurst(t);
				job.removeFirst();
				if (!job.isEmpty())
					t = job.peek();
			}
			
			return;

			
		} else
			System.out.println("the job is empty ");
	}

	public boolean deadlock() {
		if (ready.deadlock()) { ///// if there is a deadlock we have to handle it
			return ready.RemoveTheBiggestSize();//// remove the biggest process size
		}
		return false;
	}

	public boolean AdditionalMemory(int Additional) {

		if (AdditionalMemory.AdditionalMemory(Additional))
			return true;

		return false;
		
	}

	public boolean FreeMemory(int free) {

		if (AdditionalMemory.freeMemory(free))
			return true;

		return false;
	}

	public void runCycle(int time) {

		Random r = new Random();// interrupts
		internalClock++;

		Thread newThread = new Thread("OS") {
			public void run() {
				fillRAM();

			}
		};

		
		if(internalClock==time) {
			newThread.start();
			time = internalClock+200;
		}

		if (runningProcess == null) {
			if (!ready.isEmpty()) {
				runningProcess = ready.removeFirst();
				runningProcess.CPUrunning++;
				runningProcess.status = "RUNNING";
				msgs += internalClock + " " + runningProcess.toString() + " --gets CPU and start running\n";
			}
		} else {
			if (!ready.isEmpty()) {
				if (ready.peek().CPUburst < runningProcess.CPUburst) {
					runningProcess.preempted++;
					ready.insertByBurst(runningProcess);
					runningProcess = ready.removeFirst();
					msgs += internalClock + " " + runningProcess.toString() + " --gets CPU and start running\n";
				}
			}
			runningProcess.executeInstruction();
			cputime++;
			if (runningProcess.finishedExecution()) {
				runningProcess.status = "TERMINATED";
				runningProcess.finishTime = internalClock;
				msgs += internalClock + " " + runningProcess.toString() + " --finished normally by ending cpu burst\n";
				normCnt++;
				if (runningProcess.CPUbound)
					CPUcnt++;

				fileResult += runningProcess.print() + "\n";
				runningProcess = null;
			}

		}

		if (IODevice == null) {
			if (!IOQueue.isEmpty()) {
				IODevice = IOQueue.removeFirst();
				IODevice.status = "WAITING";
				msgs += internalClock + " " + IODevice.toString() + "IO --gets IO and start IO process\n";
				ioTimer = r.nextInt(60 - 20) + 20;
				cputime--;
				IODevice.IOperforming += ioTimer;
			}
		} else {
			ioTimer--;
			if (ioTimer == 0) {
				IOFREE = r.nextInt(20 - 5) + 5;
				IODevice.size -= IOFREE;
				cputime--;
				while (IODevice.size <= 5) {
					IODevice.size++;

				}

				if (ready.acceptReqSize(IODevice.getSize())){
					ready.insertByBurst(IODevice);
					IODevice.status = "READY";
					msgs += internalClock + " " + IODevice.toString() + " IO--finished IO and go to ready Queue free "
							+ IOFREE + "MB of the allocated memory\n";
					FreeMemory(IOFREE);
				} else{
					deadlock(); /// check for deadlock
					job.insertBySize(IODevice);
					IODevice.waitingMemory++;
					IODevice.status = " ";
					msgs += internalClock + " " + IODevice.toString() + " IO--finished IO and go to JOB Queue free "
							+ IOFREE + "MB of the allocated memory\n";
					FreeMemory(IOFREE);

				}
				IODevice = null;
			}
		}

		if (r.nextInt(10) == 1)// The possibility that there are interrupts is 10%
		{
			if (r.nextInt(5) == 3)// The possibility that there is an IO request is 20%
			{
				if (runningProcess != null) {
					Addsize = r.nextInt(10 - 4) + 4;
					runningProcess.size += Addsize;
					if (AdditionalMemory(Addsize)) {
						runningProcess.status = "WAITING";
						msgs += internalClock + " " + runningProcess.toString()
								+ "need IO and go to IO device Queue Additional memory required " + Addsize + "MB\n";
						runningProcess.CPUbound = false;
						runningProcess.cycle = internalClock;
						runningProcess.IOappears++;
						IOQueue.insert(runningProcess);
						if (IODevice == null) {
							ioTimer = r.nextInt(60 - 20) + 20;
							IODevice = IOQueue.removeFirst();
							IODevice.IOperforming += ioTimer;
							msgs += internalClock + " " + IODevice.toString() + "IO --gets IO and start IO process\n";

						}
					
					}
					else
					runningProcess.status = "WAITING for additional memory";
					runningProcess.waitingMemory++;
					runningProcess = null;
					
				}

			}

			if (r.nextInt(5) == 2) {
				if (IODevice != null && IODevice.cycle != internalClock) {

					if (ready.acceptReqSize(IODevice.getSize())) {
						IODevice.status = "READY";
						ready.insertByBurst(IODevice);
						cputime--;
						msgs += internalClock + " " + IODevice.toString() + " finished IO and go to ready Queue--\n";
					} else {
						job.insertBySize(IODevice);
						IODevice.status = " ";
						cputime--;
						IODevice.waitingMemory++;
						msgs += internalClock + " " + IODevice.toString() + " finished IO and go to ready Queue--\n";

					}
					IODevice = null;
					ioTimer = 0;

				}
			}
			if (r.nextInt(20) == 17) {
				if (runningProcess != null && runningProcess.CPUburst == 1) {
					runningProcess.executeInstruction();
					runningProcess.status = "TERMINATED";
					runningProcess.finishTime = internalClock;
					msgs += internalClock + " " + runningProcess.toString()
							+ " zero cpu burst finished normally by interrupt\n";
					normCnt++;

					if (runningProcess.CPUbound)
						CPUcnt++;
					fileResult += runningProcess.print() + "\n";
					runningProcess = null;
				}
			}
			if (r.nextInt(10) == 1) {
				if (runningProcess != null) {
					deadlock();
					runningProcess.status = "KILLED";
					runningProcess.waitingMemory++;
					runningProcess.finishTime = internalClock;
					runningProcess.CPUrunning++;
					msgs += internalClock + " " + runningProcess.toString() + " finished abnormally by interrupt\n";
					abnormCnt++;
					if (runningProcess.CPUbound)
						CPUcnt++;
					fileResult += runningProcess.print() + "\n";
					runningProcess = null;
				}
			}

		}

	}

	public boolean finished() {
		if (ready.isEmpty() && job.isEmpty() && IOQueue.isEmpty() && IODevice == null && runningProcess == null) {
			double CPUUT = (double) cputime / internalClock * 100;
			result += "3. The average number of jobs that have completed their execution normally are:" + normCnt
					+ " job\n" + "4. The average number of jobs that have completed their execution abnormally are: "
					+ abnormCnt + "job\n" + "5. CPU Utilization %" + CPUUT;

			fileResult += "\nID:process id " + "\n" + "\nAR:Arrival" + "\n" + "\nNOC:Number of times it was in the CPU"
					+ "\n" + "\nTSICPU:Total time spent in the CPU" + "\n" + "\nNIO:Number of times it performed an IO"
					+ "\n" + "\nPIO:Total time spent in performing IO" + "\n"
					+ "\nNOWM:Number of times it was waiting for memory" + "\n" + "\nNOP:Number of times its preempted"
					+ "\n" + "\nTTOK:Time it terminated or was killed" + "\n" + "\nFT:finishTime" + "" + "\n";
			
			File outFile = new File("Output.txt");
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(outFile));
				writer.write(fileResult);
				writer.newLine();
				writer.write(result);
				writer.close();
			} catch (IOException ex) {
				System.out.println("Error writing file");
			}

			return true;
		}
		return false;
	}

}
