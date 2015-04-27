package com.ebay.myriad.scheduler;

import org.apache.curator.framework.CuratorFramework
import org.apache.mesos.Protos.Status
import org.apache.mesos.Protos.TaskID
 
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.Service.State
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Scopes
import spock.lang.*
import static org.junit.Assert.*;
  

class MyriadHAServiceSpec extends Specification {

    // This is to be run manually from the command line, run two then kill
    // one to see if leader election occurs.
    //
    @Ignore
    def "Connect to zk" () {
        
        setup:
            def dm = new DriverModule()
            def injector = Guice.createInjector(dm)     
            def HAService haService = injector.getInstance(HAService.class)
            assert haService != null
            System.out.println("Setup");
            
        when:
            System.out.println("startup");
            haService.startUp()
            
        then:
            sleep()
            
            System.out.println("isRunning");
            
            while (haService.isRunning()) {
                sleep()
            }
                
            System.out.println("isRunning no");
                
         
    }
    // This test runs two threads, figures which is the leader, kills it,
    // then checks to see if the other becomes a leader, then kills that one.
    // Make sure the RM is not running
    def "testLeaderElectionWithFailure" () {
        setup:
            def t1 = new ServiceThread("1")
            def t2 = new ServiceThread("2")
    
        expect:
            // Allow services time to startup
            sleep()
        
            // Check to make sure they are in fact running
            assert t1.isRunning()
            assert t2.isRunning()
            
            // Make sure one is a leader
            assert t1.isLeader() || t2.isLeader()
       
            // Kill the one that is a leader and see if the other becomes a leader
            if (t1.isLeader()) {
                testServiceLeaderElection (t1, t2)
            } else if (t2.isLeader()) {
                testServiceLeaderElection (t2, t1)
            }       
    }
    
    public void testServiceLeaderElection(ServiceThread leader, ServiceThread backup) {
         try {
            int cnt = 0;
            
            System.out.println("Service " + leader.testId + " is a leader, terminating");
      
            leader.stopService();
            
            while (leader.isRunning()) {
                sleep();
            }
            System.out.println("is Service " + backup.testId + " a leader?");
      
            if (!backup.isLeader()) {
                 while (++cnt < 5 && !backup.isLeader()) {
                    sleep();
                }
    
            } 
            System.out.println("Service " + backup.testId + " is a leader, terminating");
            
            backup.stopService();
            assert cnt < 5
            
        } catch (Exception e) {
            e.printStackTrace();
            leader.stopService();
            backup.stopService();
            fail();
        }
    }  
    
    
    public void sleep() {
        try { Thread.sleep(2000); } catch (Exception e) { 
        }       
    }
    
    
    
    /**
     * 
     * Create thread to run HA Service
     *
     */
    static class ServiceThread extends Thread {
        private Injector injector;
        private MyriadHAService haService;
        protected String testId;
        
        public ServiceThread(String id) {
            DriverModule dm = new DriverModule();
            injector = Guice.createInjector(dm);
            this.testId = id;
            this.start();
            sleep(500);
        }
        public boolean isLeader() {
            return haService.isLeader();
        }
        public void stopService() {
            haService.triggerShutdown();
        }
        public boolean isRunning() {
            return (haService != null ? haService.isRunning() : false );
        }
        public void run() {
             haService = injector.getInstance(MyriadHAService.class);
           
            assert haService != null
            try {
                System.out.println("Starting service " + testId);
                haService.startUp();
                while(isRunning()) {
                    sleep(500);
                }
                    
                //haService.awaitTerminated();
            
            } catch (Exception e) {
            
                System.err.println ( "Service Failed " + e.getMessage());
                fail();
            }
            System.out.println("ServiceThread ending " + testId);         
        }
    }   
    
 
    /**
     * 
     * Test Injector Module
     *
     */
    static class DriverModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DriverManager.class).to(MockDriverManager.class).in(Scopes.SINGLETON)
            bind(HAService).to(MyriadHAService.class).in(Scopes.SINGLETON)
            bind(CuratorFramework.class).toInstance(CuratorUtils.createFramework("localhost:2181"))
        }
    }
    
    static class MockDriverManager implements DriverManager {

        public Status startDriver() {
            System.out.println("DriverManager Starting Driver")
            return null
        }

        public Status stopDriver() {
            System.out.println("DriverManager Stopping Driver")
            return null
        }

        public Status kill(TaskID taskId) {
            System.out.println("DriverManager Killing task " + taskId) 
            return null
        }

        public Status getDriverStatus() {
            System.out.println("DriverManager Providing driver status ")
            return null
        }
        
    }
}
