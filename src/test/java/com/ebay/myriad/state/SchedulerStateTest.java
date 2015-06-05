package com.ebay.myriad.state;

import java.util.concurrent.TimeUnit;

import org.apache.mesos.state.ZooKeeperState;
import org.junit.Test;

import com.google.gson.Gson;

import static org.junit.Assert.*;

/**
 * 
 * Test SchedulerState object
 *
 */
public class SchedulerStateTest {

    // Run this 
    
    // ./gradlew test --rerun-tasks --tests com.ebay.myriad.state.SchedulerStateTest.testGrabZookeeperState
    
    // when you have started a task using the UI. If you are fast enough you can see the 
    // task transition between states. 
    
    //@Test
    public void testGrabZookeeperState() {        
        try {
            ZooKeeperState zkState = new ZooKeeperState(
                    "192.168.50.101:2181",
                    20000,
                    TimeUnit.MILLISECONDS,
                    "/myriad/MyriadAlpha");
            
            TasksState state = new TasksState(zkState); 
            
            // Grab the state from zookeeper
            StoreContext ctx = state.getTasksState();
        
            // Inspect the context
            System.out.println(new Gson().toJson(ctx));
        
            // Create a SchedulerState from the context
            SchedulerState ss = new SchedulerState(ctx);
            
            // Inspect the SchedulerState
            System.out.println(new Gson().toJson(ss));
       
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void testMesosStateStore_inMemoryState() {
        
        final SchedulerState ss = TestUtils.createTestSchedulerState_withPendingTasks(); 

        System.out.println("Creating Thread to get context");
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                TasksState tstate = ss.getTasksState();
                try {
                    // Get the context from the store location.
                    StoreContext ctx = tstate.getTasksState();

                    // Check it against the original object.
                    TestUtils.checkSerialization(ss, ctx);
                    System.out.println("Thread test of state is completed");
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
        });
        
        t.start();
        
        try {
            t.join();
        } catch (Exception e) {
            fail();
        }
      
        System.out.println("Completed testMesosStateStore ");
    }
}
