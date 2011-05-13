package mathUtil;

import java.util.LinkedList;

public class MemUsage {

	public long calculateMemoryUsage(ObjectFactory factory) {
		Object handle = factory.makeObject();
		long mem0 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		long mem1 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		handle = null;
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		mem0 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		handle = factory.makeObject();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		mem1 = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		return mem1 - mem0;
	}

	public void showMemoryUsage(ObjectFactory factory) {
		long mem = calculateMemoryUsage(factory);
		System.out.println(factory.getClass().getName() + " produced "
				+ factory.makeObject().getClass().getName() + " which took "
				+ mem + " bytes");
	}
	
	public static void main(String[] args) {
		MemUsage mm = new MemUsage();
		mm.showMemoryUsage(new ObjectFactory());
	}

}

class ObjectFactory {
	public Object makeObject() {
	    LinkedList result = new LinkedList();
	    for (int i=0; i<10000; i++) {
	      result.add(new Object());
	    }
	    return result;
	  }
}
