package org.tmf.dsmapi.freeSlot.event;

import java.util.List;
import java.util.Set;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.MultivaluedMap;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.tmf.dsmapi.commons.utils.Jackson;
import org.tmf.dsmapi.commons.utils.URIParser;
import org.tmf.dsmapi.hub.Hub;
import org.tmf.dsmapi.commons.jaxrs.RESTClient;

@Stateless
@Asynchronous
public class FreeSlotRESTEventPublisher implements FreeSlotRESTEventPublisherLocal {

    @EJB
    FreeSlotEventFacade eventFacade;

    @EJB
    RESTClient client;

    @Override
    public void publish(Hub hub, FreeSlotEvent event) {

        MultivaluedMap<String, String> query = URIParser.getParameters(hub.getQuery());
        query.putSingle("id", event.getId());

        // fields to filter view
        Set<String> fieldSet = URIParser.getFieldsSelection(query);

        List<FreeSlotEvent> resultList = null;
        resultList = eventFacade.findByCriteria(query, FreeSlotEvent.class);

        if (resultList != null && !resultList.isEmpty()) {
            if (!fieldSet.isEmpty() && !fieldSet.contains(URIParser.ALL_FIELDS)) {
                fieldSet.add("id");
                fieldSet.add("date");
                fieldSet.add("eventType");
                fieldSet.add("reason");
                ObjectNode rootNode = Jackson.createNode(event, fieldSet);
                client.publishEvent(hub.getCallback(), rootNode);
            } else {
                client.publishEvent(hub.getCallback(), event);
            }
            
        }
    }

}
