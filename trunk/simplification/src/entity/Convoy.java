package entity;

import java.util.Set;

import org.joda.time.LocalDateTime;

public class Convoy {
	public int lifetime;
	public boolean assigned;
	public LocalDateTime startTime, endTime;
	public Set<Integer> members;
}
