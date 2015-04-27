package com.ebay.myriad.scheduler;

import com.google.common.util.concurrent.Service;

/**
 * 
 * High Available service interface
 *
 */
public interface HAService extends Service {

    public boolean isLeader();
    
}
