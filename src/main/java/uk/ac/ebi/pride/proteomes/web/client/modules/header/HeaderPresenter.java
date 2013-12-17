package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.GroupUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 10:46
 */
public class HeaderPresenter extends Presenter<HeaderPresenter.ThisView>
                             implements ValidStateEvent.Handler,
                                        GroupUpdateEvent.Handler,
                                        ProteinUpdateEvent.Handler {

    public interface ThisView extends View {
        public void updateTitle(String title);
        public void updateDescription(String description);
        public void updateProperties(List<Pair<String, String>> links);
        public void clearProperties();
    }
    private boolean groupView;

    public HeaderPresenter(EventBus eventBus, ThisView view) {
        super(eventBus, view);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        groupView = event.getViewType() == ValidStateEvent.ViewType.Group;
    }

    @Override
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        List<Group> groups = event.getGroups();

        if(groupView && groups.size() > 0) {
            getView().updateTitle("Protein group " + groups.get(0).getId());
            getView().updateDescription(groups.get(0).getDescription());
            List<Pair<String, String>> proteins = new ArrayList<Pair<String, String>>();

            for(String protID : groups.get(0).getMemberProteins()) {
                proteins.add(new Pair<String, String>(protID, "protein=" + protID));
            }
            getView().updateProperties(proteins);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        List<Protein> proteins = event.getProteins();

        if(!groupView) {
            getView().updateTitle("Protein " + proteins.get(0).getAccession());
            getView().updateDescription(proteins.get(0).getDescription());
            getView().clearProperties();
        }
    }
}
