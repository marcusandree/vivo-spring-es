package com.brq.masm.aggregators;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.brq.masm.routes.CDRRoute;

public class BodyAggregateStrategy implements AggregationStrategy {

	private static Logger LOGGER = LogManager.getLogger(CDRRoute.class);

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // put order together in old exchange by adding the order from new exchange

        if (oldExchange == null) {
            // the first time we aggregate we only have the new exchange,
            // so we just return it
            return newExchange;
        }

        String oldIDs = oldExchange.getIn().getBody(String.class);
        String newID = newExchange.getIn().getBody(String.class);

        LOGGER.debug("Aggregate old orders: " + oldIDs);
        LOGGER.debug("Aggregate new order: " + newID);

        // put orders together separating by semi colon
        oldIDs = oldIDs + "," + newID;
        // put combined order back on old to preserve it
        oldExchange.getIn().setBody(oldIDs);

        // return old as this is the one that has all the orders gathered until now
        return oldExchange;
    }
}

