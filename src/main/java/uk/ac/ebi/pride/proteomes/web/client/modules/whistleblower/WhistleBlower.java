package uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ErrorOnUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 15:27
 */
public class WhistleBlower implements
                        StateChangingActionEvent.StateChangingActionHandler,
        ErrorOnUpdateEvent.ErrorOnUpdateHandler {

    public WhistleBlower(EventBus eventBus) {
        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        eventBus.addHandler(ErrorOnUpdateEvent.getType(), this);
    }
    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            Console.info("(Whistler): " + evName + " <- " + clName);
        }
    }

    @Override
    public void onUpdateErrorEvent(ErrorOnUpdateEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            Console.info("(Whistler): " + evName + "(\"" + event.getMessage() +
                    "\")" + " <- " + clName);
        }
    }
}
