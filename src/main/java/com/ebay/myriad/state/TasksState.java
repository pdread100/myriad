package com.ebay.myriad.state;

import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that represents the state of Myriad Tasks
 */
public class TasksState extends MyriadState {
 
    private static String keyTasksStateId = null;
    private static final String DEFAULT_TASKS_STATE_ID_KEY = "tasksStateId";
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksState.class);   
    
    public TasksState(State mesosState) {
        super(mesosState);
    }

    public void setTasksState(StoreContext ctx) throws Exception {
        checkKey();
        Variable tasksStore = getStoreObject().mutate(ctx.toSerializedContext().toByteArray());
        getMesosState().store(tasksStore).get();
    }
    
    public StoreContext getTasksState() throws Exception {
        checkKey();
        byte[] tasksState = getStoreObject().value();
        return StoreContext.fromSerializedBytes(tasksState);
    }
    
    private Variable getStoreObject() throws Exception {
        return getMesosState().fetch(keyTasksStateId).get();
    }
    
    private void checkKey() {
        if (keyTasksStateId == null) {
            createKey();
        }        
    }
    
    private void createKey() {
        try {
            if (getFrameworkID() != null) {
                keyTasksStateId = getFrameworkID().getValue() + "-" + DEFAULT_TASKS_STATE_ID_KEY;
            } else {
                handleLogFrameIdError();
            }
        } catch (RuntimeException e) {
            handleLogFrameIdError();
        } catch (Exception e) {
            handleLogFrameIdError();
        }
        LOGGER.info("created tasks state ID as: " + keyTasksStateId);
    }
    
    private void handleLogFrameIdError() {
        LOGGER.error("Failed to get MyriadState framewordId, defaulting TaskId state key to " + 
                DEFAULT_TASKS_STATE_ID_KEY);     
        keyTasksStateId = DEFAULT_TASKS_STATE_ID_KEY;
    }
   
}
