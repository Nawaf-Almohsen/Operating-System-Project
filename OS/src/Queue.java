import java.util.LinkedList;

public class Queue {

	LinkedList<PCB> list;
	int sizeOfQueue;
	int used;

	public Queue(int size) {
		list = new LinkedList<PCB>();
	    sizeOfQueue = size;
		used = 0;
	}

	public Queue() {
		list = new LinkedList<PCB>();
		sizeOfQueue = 1024*1024;

		used = 0;
	}

	public boolean acceptReqSize(int req) {
		return sizeOfQueue >= req;
	}

	
	public boolean AdditionalMemory(int req) {
		if(sizeOfQueue>=req) {
			Additional(req);
			return true;
		}
		return false;
	}
	private boolean Additional(int req) {
		sizeOfQueue-=req;
		return true;
	}
	public boolean freeMemory(int free) {
		sizeOfQueue+=free;
		return true;
	}


	public void insertByBurst(PCB p) {

		int i = 0;
		while (i < list.size() && list.get(i).getCpu() <= p.getCpu())
			i++;
		list.add(i, p);
		used += p.getSize();
		sizeOfQueue -= p.getSize();

	}

	public void insertBySize(PCB p) {

		int i = 0;
		while (i < list.size() && list.get(i).getSize() <= p.getSize())
			i++;
		list.add(i, p);

		used += p.getSize();
		sizeOfQueue -= p.getSize();
	}

	public void insert(PCB p) {

		list.add(p);

		used += p.getSize();
		sizeOfQueue -= p.getSize();
	}

	public PCB removeFirst() {

		PCB p = list.removeFirst();
		used -= p.getSize();
		sizeOfQueue += p.getSize();

		return p;
	}
	public boolean RemoveTheBiggestSize () {
		LinkedList<PCB> copyList = new LinkedList<PCB>();
		int index = -1;
		int biggestSize=0;
		
		while (!list.isEmpty()) {
			if(list.peek().size>biggestSize) {
				index=list.indexOf(list.peek());
				biggestSize=list.peek().size;	
			}
			copyList.add(list.removeFirst());

		}
		while (!copyList.isEmpty()) {
			list.add(copyList.removeFirst());
		}
		if(index!=-1)
		list.remove(index);
		
		return true;
	}

	
	public boolean deadlock() {
		LinkedList<PCB> copyList = new LinkedList<PCB>();
		
		int dedlockStatus = 0;
		while (!list.isEmpty()) {
			if (!list.peek().status.equalsIgnoreCase("WAITING"))
				dedlockStatus = -1;
			copyList.add(list.removeFirst());

		}
		while (!copyList.isEmpty()) {
			list.add(copyList.removeFirst());

		}	
		
	
			return dedlockStatus==0;
		
	
	}

	public PCB peek() {
		return list.peek();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

}
