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
package com.hazelcast.simulator.probes.probes.impl;

import com.hazelcast.simulator.probes.probes.IntervalProbe;
import com.hazelcast.simulator.probes.probes.Result;

public abstract class AbstractSimpleProbe<R extends Result<R>, T extends IntervalProbe<R, T>> implements IntervalProbe<R, T> {

    protected long started;
    protected long durationMs;
    protected int invocations;

    private boolean disabled;

    @Override
    public void started() {
    }

    @Override
    public void recordValue(long latencyNanos) {
        throw new UnsupportedOperationException("This method is just supported by IntervalProbe implementations");
    }

    @Override
    public void done() {
        invocations++;
    }

    @Override
    public long getInvocationCount() {
        return invocations;
    }

    @Override
    public void startProbing(long timeStamp) {
        started = timeStamp;
    }

    @Override
    public void stopProbing(long timeStamp) {
        if (timeStamp < 0) {
            throw new IllegalArgumentException("timeStamp must be zero or positive.");
        }
        if (started == 0) {
            throw new IllegalStateException("Can't get result as probe has not been started yet.");
        }

        long stopOrNow = (timeStamp == 0 ? System.currentTimeMillis() : timeStamp);
        durationMs = stopOrNow - started;
        if (durationMs < 0) {
            throw new IllegalArgumentException("durationMs must be positive, but was " + durationMs);
        }
    }

    @Override
    public void setValues(long durationMs, int invocations) {
        if (durationMs < 1) {
            throw new IllegalArgumentException("durationMs must be positive, but was " + durationMs);
        }
        if (invocations < 1) {
            throw new IllegalArgumentException("invocations must be positive, but was " + invocations);
        }

        this.durationMs = durationMs;
        this.invocations = invocations;
    }

    @Override
    public void disable() {
        disabled = true;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }
}
