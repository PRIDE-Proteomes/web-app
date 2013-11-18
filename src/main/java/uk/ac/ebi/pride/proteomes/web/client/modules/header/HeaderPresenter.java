package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.GroupUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 07/11/13
 *         Time: 10:46
 */
public class HeaderPresenter implements Presenter,
                                        ValidStateEvent.ValidStateHandler,
                                        GroupUpdateEvent.GroupUpdateHandler,
                                        ProteinUpdateEvent.ProteinUpdateHandler {

    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View {
        public void updateTitle(String title);
        public void updateDescription(String description);
        public void updateProperties(String properties);
    }
    private final EventBus eventBus;
    private final View view;
    private boolean groupView;

    private List<Group> groups;

    public HeaderPresenter(EventBus eventBus, View view) {
        this.eventBus = eventBus;
        this.view = view;

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        groupView = event.getViewType() == ValidStateEvent.ViewType.Group;
    }

    @Override
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        groups = event.getGroups();

        if(groupView && groups.size() > 0) {
            view.updateTitle("Protein group: " + groups.get(0).getId());
            view.updateDescription(groups.get(0).getDescription());
            StringBuilder proteins = new StringBuilder("");
            for(String protID : groups.get(0).getMemberProteins()) {
                if(!proteins.toString().equals("")) {
                    proteins.append(", ");
                }
                proteins.append(protID);
            }
            view.updateProperties(proteins.toString());
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        List<Protein> proteins = event.getProteins();

        if(!groupView) {
            view.updateTitle("Protein: " + proteins.get(0).getAccession());
            view.updateDescription(proteins.get(0).getDescription());
            view.updateProperties("");
        }
    }
}
