package algo;

public class Util {
	public static boolean isMember(Object o, Object[] objArr){
		for(Object o1 : objArr){
			if(o1.equals(o)){
				return true;
			}
		}
		return false;
	}
}
