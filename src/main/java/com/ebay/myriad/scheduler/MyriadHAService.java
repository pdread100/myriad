package com.ebay.myriad.scheduler;

import java.util.concurrent.CountDownLatch;
import javax.inject.Inject;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Myriad's High Available service 
 *
 */
public class MyriadHAService implements HAService {
    
    @Inject
    public MyriadHAService (CuratorFramework leadershipFramework,
                            DriverManager driverManager){
        
        this.leadershipFramework = leadershipFramework;
        this.driverManager = driverManager;
    }
    
    @Override 
    public boolean isLeader() {
        return getLeader();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    /**
     * This is executed by the Service#startAsync(), in the Myriad Main.class
     */
    @Override
    public void startUp() throws Exception {
        LOGGER.info("Starting HA service......");
  
        final MyriadHAService service = this;
        
        // Catch extreme terminations to remove leadership.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    service.triggerShutdown();
                } catch (Exception e) {
                }
            }
        });
       
        internalThread = new Thread(this);
        internalThread.setDaemon(true);
        internalThread.start();
    }
    
    @Override
    public void run() {
        
        try {
             LOGGER.info("HA service running.......");
             
             setLeader(false);
             
             leader = new LeaderLatch(leadershipFramework, CuratorUtils.LATCH_PATH);
              
             leader.start();
             
             // Wait till we become leader.
             leader.await();
             
             electLeadership();
        
             // run till we loose leadership, then terminate.
             latch.await();
             
        } catch (Exception e) {
            LOGGER.error("Exception  ", e);
        }
        LOGGER.info("HA service exiting.......");
     
    }

    @Override
    public void triggerShutdown() {
       
        driverManager.stopDriver();
        try {
            // Give up leadership
            leader.close();
        } catch (Exception e) {
            LOGGER.error("Leadership latch close failed ", e);
        } finally {
            setLeader(false);
        }
        
        // The countdown latch blocks run() from exiting. Counting down the latch removes the block.
        latch.countDown(); 
    }
    @Override
    public boolean isRunning() {
        return internalThread.isAlive();
    }
    protected void electLeadership() {
        LOGGER.info("MyriadHAService: electLeadership......");
        setLeader(true);
        driverManager.startDriver();
    }
    
    protected void setLeader(boolean leader) {
        isLeader = leader;
    }
    protected boolean getLeader() {
        return isLeader;
    }
    
    ///////////////////////////////////////////////////////////////////////////
   
    private LeaderLatch leader;
    private CountDownLatch latch = new CountDownLatch(1);
    private DriverManager driverManager;
    private CuratorFramework leadershipFramework;
    private boolean isLeader;
    private Thread internalThread;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadHAService.class);

    
    
    
   
}
