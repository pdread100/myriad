/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.policy.LeastAMNodesFirstPolicy;
import com.ebay.myriad.policy.NodeScaleDownPolicy;
import com.ebay.myriad.scheduler.MyriadDriverManager;
import com.ebay.myriad.scheduler.MyriadScheduler;
import com.ebay.myriad.scheduler.fgs.NMHeartBeatHandler;
import com.ebay.myriad.scheduler.NMProfileManager;
import com.ebay.myriad.scheduler.fgs.NodeStore;
import com.ebay.myriad.scheduler.fgs.OfferLifecycleManager;
import com.ebay.myriad.scheduler.ReconcileService;
import com.ebay.myriad.scheduler.TaskFactory;
import com.ebay.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import com.ebay.myriad.scheduler.fgs.YarnNodeCapacityManager;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.ebay.myriad.state.MyriadState;
import com.ebay.myriad.state.SchedulerState;
import com.ebay.myriad.webapp.HttpConnectorProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.mesos.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Guice Module for Myriad
 */
public class MyriadModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadModule.class);

    private MyriadConfiguration cfg;
    private Configuration hadoopConf;
    private AbstractYarnScheduler yarnScheduler;
    private final RMContext rmContext;
    private InterceptorRegistry interceptorRegistry;

    public MyriadModule(MyriadConfiguration cfg,
                        Configuration hadoopConf,
                        AbstractYarnScheduler yarnScheduler,
                        RMContext rmContext,
                        InterceptorRegistry interceptorRegistry) {
        this.cfg = cfg;
        this.hadoopConf = hadoopConf;
        this.yarnScheduler = yarnScheduler;
        this.rmContext = rmContext;
        this.interceptorRegistry = interceptorRegistry;
    }

    @Override
    protected void configure() {
        LOGGER.debug("Configuring guice");
        bind(MyriadConfiguration.class).toInstance(cfg);
        bind(Configuration.class).toInstance(hadoopConf);
        bind(RMContext.class).toInstance(rmContext);
        bind(AbstractYarnScheduler.class).toInstance(yarnScheduler);
        bind(InterceptorRegistry.class).toInstance(interceptorRegistry);
        bind(MyriadDriverManager.class).in(Scopes.SINGLETON);
        bind(MyriadScheduler.class).in(Scopes.SINGLETON);
        bind(NMProfileManager.class).in(Scopes.SINGLETON);
        bind(DisruptorManager.class).in(Scopes.SINGLETON);
        bind(TaskFactory.class).to(NMTaskFactoryImpl.class);
        bind(ReconcileService.class).in(Scopes.SINGLETON);
        bind(HttpConnectorProvider.class).in(Scopes.SINGLETON);
        bind(YarnNodeCapacityManager.class).in(Scopes.SINGLETON);
        bind(NodeStore.class).in(Scopes.SINGLETON);
        bind(OfferLifecycleManager.class).in(Scopes.SINGLETON);
        bind(NMHeartBeatHandler.class).asEagerSingleton();

        //TODO(Santosh): Should be configurable as well
        bind(NodeScaleDownPolicy.class).to(LeastAMNodesFirstPolicy.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    SchedulerState providesSchedulerState(MyriadConfiguration cfg,
        State stateStore) {

        LOGGER.debug("Configuring SchedulerState provider");
        MyriadState state = new MyriadState(stateStore);
        return new SchedulerState(state);
    }
}
