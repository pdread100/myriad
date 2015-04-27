package com.ebay.myriad.scheduler;

import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;

/**
 * 
 * Driver api
 *
 */
public interface DriverManager {
    
    /**
     * Start the driver
     * 
     * @return status 
     */
    public Status startDriver();
    
    /**
     * Stop the driver
     * 
     * @return status
     */
    public Status stopDriver();
    
    /**
     * Kill a task
     * 
     * @param taskId
     * @return status
     */
    public Status kill(final TaskID taskId);
    
    /**
     * Get the status
     * 
     * @return status
     */
    public Status getDriverStatus();

}
