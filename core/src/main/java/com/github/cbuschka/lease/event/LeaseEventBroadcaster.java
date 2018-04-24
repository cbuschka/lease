package com.github.cbuschka.lease.event;

import java.util.ArrayList;
import java.util.List;

public class LeaseEventBroadcaster
{
	private List<LeaseEventListener> leaseEventListeners = new ArrayList<>();

	public void setLeaseEventListeners(List<LeaseEventListener> leaseEventListeners)
	{
		this.leaseEventListeners = leaseEventListeners;
	}

	public void broadcast(LeaseEvent event)
	{
		for (LeaseEventListener curr : leaseEventListeners)
		{
			curr.onLeaseEvent(event);
		}
	}
}
