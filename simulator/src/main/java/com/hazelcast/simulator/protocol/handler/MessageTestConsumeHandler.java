package com.hazelcast.simulator.protocol.handler;

import com.google.gson.Gson;
import com.hazelcast.simulator.protocol.core.Response;
import com.hazelcast.simulator.protocol.core.SimulatorAddress;
import com.hazelcast.simulator.protocol.core.SimulatorMessage;
import com.hazelcast.simulator.protocol.operation.SimulatorOperation;
import com.hazelcast.simulator.protocol.operation.SimulatorOperationFactory;
import com.hazelcast.simulator.protocol.processors.OperationProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hazelcast.simulator.protocol.core.AddressLevel.TEST;
import static com.hazelcast.simulator.protocol.core.ResponseType.FAILURE_TEST_NOT_FOUND;
import static java.lang.String.format;

/**
 * A {@link SimpleChannelInboundHandler} to to deserialize a {@link SimulatorOperation} from a received {@link SimulatorMessage}
 * and execute it on the {@link OperationProcessor} of the addressed Simulator Test.
 */
public class MessageTestConsumeHandler extends SimpleChannelInboundHandler<SimulatorMessage> {

    private static final Logger LOGGER = Logger.getLogger(MessageTestConsumeHandler.class);

    private final Gson gson = new Gson();
    private final AttributeKey<Integer> addressIndex = AttributeKey.valueOf("addressIndex");
    private final ConcurrentMap<Integer, SimulatorAddress> testAddresses = new ConcurrentHashMap<Integer, SimulatorAddress>();
    private final ConcurrentMap<Integer, OperationProcessor> testProcessors
            = new ConcurrentHashMap<Integer, OperationProcessor>();

    private final SimulatorAddress localAddress;
    private final int agentIndex;
    private final int workerIndex;

    public MessageTestConsumeHandler(SimulatorAddress localAddress) {
        this.localAddress = localAddress;
        this.agentIndex = localAddress.getAgentIndex();
        this.workerIndex = localAddress.getWorkerIndex();
    }

    public void addTest(int testIndex, OperationProcessor processor) {
        SimulatorAddress testAddress = new SimulatorAddress(TEST, agentIndex, workerIndex, testIndex);
        testAddresses.put(testIndex, testAddress);
        testProcessors.put(testIndex, processor);
    }

    public void removeTest(int testIndex) {
        testAddresses.remove(testIndex);
        testProcessors.remove(testIndex);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SimulatorMessage msg) {
        LOGGER.debug(format("[%d] %s MessageTestConsumeHandler is consuming message...", msg.getMessageId(), localAddress));
        SimulatorOperation operation = SimulatorOperationFactory.fromJson(gson, msg);

        Response response = new Response(msg.getMessageId());
        int testAddressIndex = ctx.attr(addressIndex).get();
        if (testAddressIndex == 0) {
            LOGGER.debug(format("[%d] forwarding message to all tests", msg.getMessageId()));
            for (Map.Entry<Integer, OperationProcessor> entry : testProcessors.entrySet()) {
                response.addResponse(testAddresses.get(entry.getKey()), entry.getValue().process(operation));
            }
        } else {
            LOGGER.debug(format("[%d] forwarding message to test %d", msg.getMessageId(), testAddressIndex));
            OperationProcessor processor = testProcessors.get(testAddressIndex);
            if (processor == null) {
                response.addResponse(localAddress, FAILURE_TEST_NOT_FOUND);
            } else {
                response.addResponse(testAddresses.get(testAddressIndex), processor.process(operation));
            }
        }
        ctx.writeAndFlush(response);
    }
}
