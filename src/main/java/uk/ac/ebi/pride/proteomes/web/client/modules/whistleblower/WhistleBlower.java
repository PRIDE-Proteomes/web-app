package uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.TextUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 15:27
 */
public class WhistleBlower implements
                        StateChangingActionEvent.StateChangingActionHandler,
                        TextUpdateEvent.TextUpdateHandler  {

    public WhistleBlower(EventBus eventBus) {
        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        eventBus.addHandler(TextUpdateEvent.getType(), this);
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
    public void onTextUpdateEvent(TextUpdateEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            Console.info("(Whistler): " + evName + "(\"" + event.getText() +
                    "\")" + " <- " + clName);
        }
    }
}
