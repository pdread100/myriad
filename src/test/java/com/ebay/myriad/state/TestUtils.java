package com.ebay.myriad.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.state.InMemoryState;

import com.ebay.myriad.scheduler.NMProfile;
import com.google.protobuf.ByteString;

/**
 * 
 * Utilities to help test state
 *
 */
public class TestUtils {
    
    public static SchedulerState createTestSchedulerState_withPendingTasks() {
        SchedulerState ss = new SchedulerState(new TasksState(new InMemoryState()));
        ArrayList<NodeTask> nodes = new ArrayList<NodeTask>(3);        
        SlaveID sid = SlaveID.newBuilder().setValue("SlaveID-123").build();

        for (int i = 0; i < 3; i++) {
            NodeTask ntask  = new NodeTask(new NMProfile("x" + i, 2L, 2000L));
            ntask.setSlaveId(sid);          
            ntask.setTaskStatus(newTaskStatus(TaskState.TASK_LOST, "TSK" + i, sid));
            nodes.add(ntask);
        }
        ss.addNodes(nodes);

        return ss;
    }
    
    public static TaskStatus newTaskStatus(TaskState state, String taskId, SlaveID sid) {
        return Protos.TaskStatus.newBuilder()
                .setTaskId(TaskID.newBuilder().setValue(taskId))
                .setState(state)
        .setData(ByteString.copyFromUtf8("This brown Cow"))
        .setExecutorId(ExecutorID.newBuilder().setValue("executorId-123"))
        .setSlaveId(sid).build();
    }
    
    @SuppressWarnings("unchecked")
    public static void checkSerialization(SchedulerState ss, StoreContext ctx) {
        Map<Protos.TaskID, NodeTask> orgTasksMap = ss.getTasks();
                
        // Make sure the context matches the original task map
        checkTasksMapEqual(orgTasksMap, ctx.getTasks());
        
        // Now test serial/de-serial
        try {
            ByteArrayOutputStream stream = ctx.toSerializedContext();
            assertNotNull(stream);
            assertNotNull(stream.toByteArray());
            assertTrue(stream.toByteArray().length > 0);
                       
            StoreContext deserializedCtx = StoreContext.fromSerializedBytes(stream.toByteArray());
            
            checkTasksMapEqual(orgTasksMap, deserializedCtx.getTasks());
     
            
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
               
    }
    
    public static void checkTasksMapEqual(Map<Protos.TaskID, NodeTask> orgTasksMap, 
            Map<Protos.TaskID, NodeTask> storedTasksMap) {
        
        for (Entry<TaskID, NodeTask> entry : orgTasksMap.entrySet()) {
            assertTrue(storedTasksMap.containsKey(entry.getKey()));
            NodeTask ont = entry.getValue();
            NodeTask snt = storedTasksMap.get(entry.getKey());
            NMProfile oprof = ont.getProfile();
            NMProfile sprof = snt.getProfile();

            assertEquals(oprof.getCpus(), sprof.getCpus());
            assertEquals(oprof.getMemory(), sprof.getMemory());
            assertEquals(oprof.getName(), sprof.getName());
            assertTrue(ont.getSlaveId().equals(snt.getSlaveId()));
            assertTrue(ont.getTaskStatus().equals(snt.getTaskStatus()));
        }
             
    }   
    
}
