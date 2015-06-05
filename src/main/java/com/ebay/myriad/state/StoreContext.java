package com.ebay.myriad.state;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskID;

/**
 * The purpose of this container/utility is to create a mechanism to serialize the SchedulerState
 * to zookeeper and back. Json did not seem to handle the Protos fields very well so this was an
 * alternative approach.
 *
 */
public final class StoreContext {
    
    private List<ByteBuffer> taskIds;
    private List<ByteBuffer> taskNodes;
    private List<ByteBuffer> pendingTasks;
    private List<ByteBuffer> stagingTasks;
    private List<ByteBuffer> activeTasks;
    private List<ByteBuffer> lostTasks;
    private List<ByteBuffer> killableTasks;   
    
    public StoreContext() {
    }
    
    /**
     * Accept all the SchedulerState maps and flatten them into lists of ByteBuffers
     * @param tasks
     * @param pendingTasks
     * @param stagingTasks
     * @param activeTasks
     * @param lostTasks
     * @param killableTasks
     */
    public StoreContext(Map<Protos.TaskID, NodeTask> tasks,
            Set<Protos.TaskID> pendingTasks, Set<Protos.TaskID> stagingTasks,
            Set<Protos.TaskID> activeTasks, Set<Protos.TaskID> lostTasks,
            Set<Protos.TaskID> killableTasks) {
        setTasks(tasks);
        setPendingTasks(pendingTasks);
        setStagingTasks(stagingTasks);
        setActiveTasks(activeTasks);
        setLostTasks(lostTasks);
        setKillableTasks(killableTasks);
    }
   
    /**
     * Accept list of ByteBuffers and re-create the SchedulerState maps.
     * @param taskIds
     * @param taskNodes
     * @param pendingTasks
     * @param stagingTasks
     * @param activeTasks
     * @param lostTasks
     * @param killableTasks
     */
    public StoreContext(List<ByteBuffer> taskIds, List<ByteBuffer> taskNodes, 
            List<ByteBuffer> pendingTasks, List<ByteBuffer> stagingTasks, List<ByteBuffer> activeTasks,
            List<ByteBuffer> lostTasks, List<ByteBuffer> killableTasks) {
        this.taskIds = taskIds;
        this.taskNodes = taskNodes;
        this.pendingTasks = pendingTasks;
        this.stagingTasks = stagingTasks;
        this.activeTasks = activeTasks;
        this.lostTasks = lostTasks;
        this.killableTasks = killableTasks;
    }
    
    /**
     * Use this to gather bytes to push to the real store, i.e. zookeeper
     * @return byte stream of the state store context.
     * @throws IOException
     */
    public ByteArrayOutputStream toSerializedContext() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteBufferSupport.addByteBuffers(taskIds, bytes);
        ByteBufferSupport.addByteBuffers(taskNodes, bytes);
        ByteBufferSupport.addByteBuffers(pendingTasks, bytes);
        ByteBufferSupport.addByteBuffers(stagingTasks, bytes);
        ByteBufferSupport.addByteBuffers(activeTasks, bytes);
        ByteBufferSupport.addByteBuffers(lostTasks, bytes);
        ByteBufferSupport.addByteBuffers(killableTasks, bytes);
        
        return bytes;
    }
    
    /**
     * When the bytes come back from the store, use this method to create a new context.
     * 
     * @param bytes from state store
     * @return initialized StoreContext to use to initialize a SchedulerState
     */
    @SuppressWarnings("unchecked")
    public static StoreContext fromSerializedBytes(byte bytes[]) {
        StoreContext ctx;
        
        if (bytes != null && bytes.length > 0){
            ByteBuffer bb = ByteBufferSupport.fillBuffer(bytes);
            List<ByteBuffer> taskIds = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> taskNodes = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> pendingTasks = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> stagingTasks = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> activeTasks = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> lostTasks = ByteBufferSupport.createBufferList(bb, bb.getInt());
            List<ByteBuffer> killableTasks = ByteBufferSupport.createBufferList(bb, bb.getInt());
            ctx = new StoreContext(taskIds, taskNodes, pendingTasks, stagingTasks, activeTasks,
                    lostTasks, killableTasks);
        } else {
            ctx = new StoreContext();
        }
        return ctx;
    }
    
    /**
     * Serialize tasks into internal ByteBuffers, removing the map.
     * 
     * @param tasks
     */
    public void setTasks(Map<Protos.TaskID, NodeTask> tasks) {        
        taskIds = new ArrayList<ByteBuffer>(tasks.size());
        taskNodes = new ArrayList<ByteBuffer>(tasks.size());
        for (Entry<TaskID, NodeTask> entry : tasks.entrySet()) {
            taskIds.add(ByteBufferSupport.toByteBuffer(entry.getKey()));
            taskNodes.add(ByteBufferSupport.toByteBuffer(entry.getValue()));
        }
    }
 
    /**
     * De-serialize the internal ByteBuffers back into a Task map.
     * 
     * @return
     */
    public Map<Protos.TaskID, NodeTask> getTasks() {
        Map<Protos.TaskID, NodeTask> map = null;
        if (taskIds != null) {
            map = new HashMap<Protos.TaskID, NodeTask>(taskIds.size());
            int idx = 0;
            for (ByteBuffer bb : taskIds) {
                map.put(ByteBufferSupport.toTaskId(bb), ByteBufferSupport.toNodeTask(taskNodes.get(idx++)));
            }
        } else {
            map = new HashMap<Protos.TaskID, NodeTask>(0);
        }
        return map;
    }
    
    public void setPendingTasks(Set<Protos.TaskID> tasks) {
        if (tasks != null) {
            pendingTasks = new ArrayList<ByteBuffer>(tasks.size());
            toTaskBuffer(tasks, pendingTasks);
        }
    }
    
    public Set<Protos.TaskID> getPendingTasks () {
        return toTaskSet(pendingTasks);
    }
    
    public void setStagingTasks(Set<Protos.TaskID> tasks) {
        if (tasks != null) {
            stagingTasks = new ArrayList<ByteBuffer>(tasks.size());
            toTaskBuffer(tasks, stagingTasks);
        }
    }
    
    public Set<Protos.TaskID> getStagingTasks() {
        return toTaskSet(stagingTasks);
    }
    
    public void setActiveTasks(Set<Protos.TaskID> tasks) {
        if (tasks != null) {
            activeTasks = new ArrayList<ByteBuffer>(tasks.size());
            toTaskBuffer(tasks, activeTasks);   
        }
    }
    
    public Set<Protos.TaskID> getActiveTasks() {
        return toTaskSet(activeTasks);
    }
    
    public void setLostTasks(Set<Protos.TaskID> tasks) {
        if (tasks != null) {
            lostTasks = new ArrayList<ByteBuffer>(tasks.size());
            toTaskBuffer(tasks, lostTasks);
        }
    }
    
    public Set<Protos.TaskID> getLostTasks() {
        return toTaskSet(lostTasks);
    }
    
    public void setKillableTasks(Set<Protos.TaskID> tasks) {
        if (tasks != null) {
            killableTasks = new ArrayList<ByteBuffer>(tasks.size());
            toTaskBuffer(tasks, killableTasks);
        }
    }
    
    public Set<Protos.TaskID> getKillableTasks() {
        return toTaskSet(killableTasks);
    }
    
 
    private void toTaskBuffer(Set<Protos.TaskID> src, List<ByteBuffer> tgt) {
         for (Protos.TaskID id : src) {
            tgt.add(ByteBufferSupport.toByteBuffer(id));
        }
    }
   
    private Set<Protos.TaskID> toTaskSet(List<ByteBuffer> src) {
        Set<Protos.TaskID> tasks;
        
        if (src != null) {
            tasks = new HashSet<Protos.TaskID>(src.size());
            for (int i = 0; i < src.size(); i++) {
                tasks.add(ByteBufferSupport.toTaskId(src.get(i)));
            }
        } else {
            tasks = new HashSet<Protos.TaskID>(0);
        }
        return tasks;      
    }

}
