package com.ebay.myriad.scheduler;


/**
 * 
 * High Available service interface
 *
 * I would have preferred to use Google's Service but the hadoop guava
 * libs are way out of date which makes it impossible to do.
 * 
 */
public interface HAService extends Runnable {

    public boolean isLeader();
    
    /**
     * Start up the service
     * 
     * @throws Exception
     */
    public void startUp() throws Exception;
    
    /**
     * Shutdown the service
     */
    public void triggerShutdown();
    
    
    /**
     * Is this service running
     */
    
    public boolean isRunning();
}
