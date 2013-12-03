package uk.ac.ebi.pride.proteomes.web.client.modules.googleanalytics;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 02/12/13
 *         Time: 16:06
 */
public class Reporter implements
        StateChangingActionEvent.StateChangingActionHandler,
        ValueChangeHandler
{
    private String state = "";
    public Reporter(EventBus eventBus) {
        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        eventBus.addHandler(ValueChangeEvent.getType(), this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onValueChange(ValueChangeEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
