package com.github.cbuschka.lease.event;

public class LeaseEvent
{
	private LeaseEventType eventType;

	private String leaseName;

	public LeaseEvent(LeaseEventType eventType, String leaseName)
	{
		this.eventType = eventType;
		this.leaseName = leaseName;
	}

	public LeaseEventType getEventType()
	{
		return eventType;
	}

	public String getLeaseName()
	{
		return leaseName;
	}

	@Override
	public String toString()
	{
		return String.format("%s{type=%s,name=%s}", getClass().getSimpleName(),
				this.eventType.name(), this.leaseName);
	}
}
