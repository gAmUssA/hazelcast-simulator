/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.simulator.tests;

import com.hazelcast.simulator.test.TestContext;
import com.hazelcast.simulator.test.annotations.Performance;
import com.hazelcast.simulator.test.annotations.Run;
import com.hazelcast.simulator.test.annotations.Setup;

import static com.hazelcast.simulator.utils.CommonUtils.sleepSeconds;

public class PerformanceMonitorTest {

    private TestContext context;
    private long operations;

    @Setup
    public void setUp(TestContext context) {
        this.context = context;
    }

    @Performance
    public long getOperationCount() {
        return operations++;
    }

    @Run
    void run() {
        while (!context.isStopped()) {
            sleepSeconds(1);
        }
    }
}
