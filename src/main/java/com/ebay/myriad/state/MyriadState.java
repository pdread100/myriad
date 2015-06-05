package com.ebay.myriad.state;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

import java.util.concurrent.ExecutionException;

/**
 * Model that represents the state of Myriad
 */
public class MyriadState {
    private static final String KEY_FRAMEWORK_ID = "frameworkId";

    // Stored somewhere, don't care where.
    private State mesosState;

    public MyriadState(State mesosState) {
        this.mesosState = mesosState;
    }

    public Protos.FrameworkID getFrameworkID() throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        byte[] frameworkId = mesosState.fetch(KEY_FRAMEWORK_ID).get().value();

        if (frameworkId.length > 0) {
            return Protos.FrameworkID.parseFrom(frameworkId);
        } else {
            return null;
        }
    }

    public void setFrameworkId(Protos.FrameworkID newFrameworkId) throws InterruptedException, ExecutionException {
        Variable frameworkId = mesosState.fetch(KEY_FRAMEWORK_ID).get();
        frameworkId = frameworkId.mutate(newFrameworkId.toByteArray());
        mesosState.store(frameworkId).get();
    }
    
    /**
     * Let subclasses grab this, ie see TasksState
     * @return whatever state this object is using.
     */
    protected State getMesosState() {
        return mesosState;
    }
}
