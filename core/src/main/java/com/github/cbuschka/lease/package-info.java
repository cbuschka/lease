/**
 * <p>Spring database based lease support.</p>
 * <br>
 * <h2>Lease management</h2>
 * <ul>
 *     <li>Check if lease manamenet enabled: {@link com.github.cbuschka.lease.LeaseManagerService#isEnabled()}</li>
 *     <li>Check if a lease is acquired: {@link com.github.cbuschka.lease.LeaseManagerService#isAcquired(java.lang.String)}</li>
 * </ul>
 *
 * <h2>Lease events</h2>
 * <p>Create a bean implementing {@link com.github.cbuschka.lease.event.LeaseEventListener} to get
 * notified about lease events:</p>
 * <ul>
 *     <li>{@link com.github.cbuschka.lease.event.LeaseEventType#ACQUIRED} when lease was newly acquired</li>
 *     <li>{@link com.github.cbuschka.lease.event.LeaseEventType#RENEWED} when lease has been successfully
 *     re-acquired</li>
 *     <li>{@link com.github.cbuschka.lease.event.LeaseEventType#RELEASED} when a lease was held and
 *     and now lease is taken by someone else</li>
 * </ul>
 *
 * <h2>Configuration API</h2>
 *
 */
package com.github.cbuschka.lease;