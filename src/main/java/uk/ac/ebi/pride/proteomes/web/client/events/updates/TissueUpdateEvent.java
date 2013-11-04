package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 15:10
 */
public class TissueUpdateEvent extends
        GwtEvent<TissueUpdateEvent.TissueUpdateHandler> {

    public interface TissueUpdateHandler extends EventHandler {
        public void onTissueUpdateEvent(TissueUpdateEvent event);
    }

    private static final Type<TissueUpdateHandler> TYPE = new Type<TissueUpdateHandler>();

    private String[] tissues;

    public TissueUpdateEvent(String[] tissues, HasHandlers source) {
        super();
        this.tissues = tissues;
        setSource(source);
    }

    public static void fire(HasHandlers source, String[] tissues) {
        TissueUpdateEvent eventInstance = new TissueUpdateEvent(tissues, source);
        source.fireEvent(eventInstance);
    }

    public String[] getTissues() {
        return tissues;
    }

    public static Type<TissueUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<TissueUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TissueUpdateHandler handler) {
        handler.onTissueUpdateEvent(this);
    }
}
