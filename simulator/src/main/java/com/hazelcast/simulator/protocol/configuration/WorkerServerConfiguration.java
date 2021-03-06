package com.hazelcast.simulator.protocol.configuration;

import com.hazelcast.simulator.protocol.core.AddressLevel;
import com.hazelcast.simulator.protocol.core.SimulatorAddress;
import com.hazelcast.simulator.protocol.handler.MessageConsumeHandler;
import com.hazelcast.simulator.protocol.handler.MessageDecoder;
import com.hazelcast.simulator.protocol.handler.MessageTestConsumeHandler;
import com.hazelcast.simulator.protocol.handler.ResponseEncoder;
import com.hazelcast.simulator.protocol.handler.SimulatorFrameDecoder;
import com.hazelcast.simulator.protocol.processors.OperationProcessor;
import io.netty.channel.ChannelPipeline;

import static com.hazelcast.simulator.protocol.core.AddressLevel.WORKER;

/**
 * Bootstrap configuration for a {@link com.hazelcast.simulator.protocol.connector.WorkerConnector}.
 */
public class WorkerServerConfiguration extends AbstractBootstrapConfiguration {

    private final MessageTestConsumeHandler messageTestConsumeHandler = new MessageTestConsumeHandler(localAddress);

    private final OperationProcessor processor;

    public WorkerServerConfiguration(SimulatorAddress localAddress, int addressIndex, int port, OperationProcessor processor) {
        super(localAddress, addressIndex, port);
        this.processor = processor;
    }

    @Override
    public AddressLevel getAddressLevel() {
        return WORKER;
    }

    @Override
    public void configurePipeline(ChannelPipeline pipeline) {
        pipeline.addLast("encoder", new ResponseEncoder());
        pipeline.addLast("frameDecoder", new SimulatorFrameDecoder());
        pipeline.addLast("decoder", new MessageDecoder(localAddress, WORKER));
        pipeline.addLast("consumer", new MessageConsumeHandler(localAddress, processor));
        pipeline.addLast("testDecoder", new MessageDecoder(localAddress, AddressLevel.TEST));
        pipeline.addLast("testConsumer", messageTestConsumeHandler);
    }

    public void addTest(int testIndex, OperationProcessor processor) {
        messageTestConsumeHandler.addTest(testIndex, processor);
    }

    public void removeTest(int testIndex) {
        messageTestConsumeHandler.removeTest(testIndex);
    }
}
