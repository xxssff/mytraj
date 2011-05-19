package entity;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDateTime;

public class Convoy {
	public int lifetime;
	public boolean assigned;
	public LocalDateTime startTime, endTime;
	public Set<Integer> members;

	public Convoy() {
		assigned = false;
	}

	public Convoy(Cluster c) {
		assigned = false;
		this.members = new HashSet<Integer>(c.members);
	}

	public Convoy(Cluster c, LocalDateTime startTime, LocalDateTime endTime) {
		assigned = false;
		this.members = new HashSet<Integer>(c.members);
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String toString() {
		return lifetime + " " + startTime + " " + endTime + " " + members;

	}

	public boolean equals(Convoy aConvoy) {
		return (this.members.equals(aConvoy.members));
	}
}
