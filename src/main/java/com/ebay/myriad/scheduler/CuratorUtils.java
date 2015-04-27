package com.ebay.myriad.scheduler;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 
 * Curator helper methods.
 *
 */
public class CuratorUtils {
    
    public static final String NAMESPACE = "MyriadHA";
    public static final String LATCH_PATH = "/LatchLeadership";
   
    
    /**
     * Create a CuratorFramework for leadership handling. 
     * 
     * @param connectionString
     * @return a started Curator framework
     */
    public static CuratorFramework createFramework(String connectionString){
        
        // these are reasonable arguments for the ExponentialBackoffRetry. The first
        // retry will wait 1 second - the second will wait up to 2 seconds - the
        // third will wait up to 4 seconds.
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // The simplest way to get a CuratorFramework instance. This will use default values.
        // The only required arguments are the connection string and the retry policy
        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .namespace(NAMESPACE)
                .build();
        
        framework.start();
        
        return framework;
    }

    /**
     * Create a started CuratorFramework for leadership handling. 
     * 
     * @param connectionString
     * @param retryPolicy
     * @param connectionTimeoutMs
     * @param sessionTimeoutMs
     * @return a started Curator framework
     */
    public static CuratorFramework createFramework(String connectionString, 
                                                   RetryPolicy retryPolicy, 
                                                   int connectionTimeoutMs, 
                                                   int sessionTimeoutMs){
        
        
        // using the CuratorFrameworkFactory.builder() gives fine grained control
        // over creation options. See the CuratorFrameworkFactory.Builder javadoc
        // details
         
        CuratorFramework framework =  CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .namespace(NAMESPACE)
                .build();
        
        framework.start();
        
        return framework;     
        
    }
}
