package uk.ac.ebi.pride.proteomes.web.client.modules;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/12/13
 *         Time: 15:00
 */
public class EventSender implements HasHandlers {
    private final EventBus eventBus;

    public EventSender(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }
}
