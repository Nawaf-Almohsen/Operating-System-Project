public class PCB {
	
	int id;// process id
	int CPUburst; // CPU/burst
	int IOappears; // IO/burst
    int size; // process size
	boolean CPUbound; // Process bound kind (IO OR CPU)
	String status; // The current status of the process
	int CPUtime; /// Number of times it was in the CPU
	int CPUrunning;//Total time spent in the CPU
	int waitingMemory;//Number of times it was waiting for memory.
	int IOperforming; //Total time spent in performing IO
	long loadedInReadyQueue; //When it was loaded into the ready queue
	long finishTime;//Time it terminated or was killed
	int preempted; //Number of times its preempted (stopped execution because another process replaced it)
	long cycle; // cycle counter


	public PCB(int id, int cpu, int size , String status) {
		this.id = id;
		CPUburst = cpu;
		this.size = size;
		CPUbound = true;
		IOappears=0;
		status=" ";
		CPUtime=0;
		CPUrunning=0;
		IOperforming=0;
		loadedInReadyQueue=0;
		finishTime=0;
		preempted=0;
		 cycle=0;
		 waitingMemory=0;

		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCpu() {
		return CPUburst;
	}

	public void setCpu(int cpu) {
		this.CPUburst = cpu;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}


	public void executeInstruction() {
		CPUtime++;
		CPUburst--;
	}

	public boolean finishedExecution() {
		return CPUburst == 0;
	}

	public String toString() {
		return "Process [id:" + id + ";cpu:" + CPUburst +";status:"+status+ ";size=" + size + "]";
	}
	
	public String print() {
				 
		String info="";
		info += id + "\t";
		info += loadedInReadyQueue+ "\t";
		info +=  CPUrunning+ "\t\t";
		info +=  CPUtime+ "\t\t";
		info += IOappears+ "\t\t";
		info += IOperforming + "\t\t";
		info += waitingMemory + "\t\t";
		info += preempted + "\t\t";
		
		info += finishTime + "\t";
		info +=status + "\t\t";
		info += "\n";
		
		return info;
	}

}
