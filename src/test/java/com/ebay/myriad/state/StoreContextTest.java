package com.ebay.myriad.state;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import org.apache.mesos.Protos;
import org.apache.mesos.state.InMemoryState;
import org.junit.Test;


/**
 * TestStoreContext
 *
 */
public class StoreContextTest {

    @Test
    public void testByteBufferSerialization_withEmptyState() {
        SchedulerState ss = new SchedulerState(new TasksState(new InMemoryState()));

        assertTrue(ss.getTasks().size() == 0);
        assertTrue(ss.getPendingTaskIds().size() == 0);
        assertTrue(ss.getStagingTaskIds().size() == 0);
        assertTrue(ss.getActiveTaskIds().size() == 0);
        assertTrue(ss.getLostTaskIds().size() == 0);
        assertTrue(ss.getKillableTasks().size() == 0);      
        
        TestUtils.checkSerialization(ss, ss.getContext());
    }
    @Test
    public void testByteBufferSerialization() {
                
        SchedulerState ss = TestUtils.createTestSchedulerState_withPendingTasks(); 
        validateScheduleState_withPendingTasks(ss);
        TestUtils.checkSerialization(ss, ss.getContext());     
    }
    


    @SuppressWarnings("unchecked")
    public void validateScheduleState_withPendingTasks(SchedulerState state) {
        Map<Protos.TaskID, NodeTask> orgTasksMap = state.getTasks();
        Set<Protos.TaskID> taskIds = state.getPendingTaskIds();
        assertEquals(orgTasksMap.size(), taskIds.size());
 
        // All tasks should be pending
        for (Protos.TaskID id :  taskIds) {
            assertTrue(orgTasksMap.containsKey(id));
        }
       
        assertTrue(state.getStagingTaskIds().size() == 0);
        assertTrue(state.getActiveTaskIds().size() == 0);
        assertTrue(state.getLostTaskIds().size() == 0);
        assertTrue(state.getKillableTasks().size() == 0);
    }
    
 
}
