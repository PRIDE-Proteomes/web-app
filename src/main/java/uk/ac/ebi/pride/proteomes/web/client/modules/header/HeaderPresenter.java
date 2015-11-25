package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.GroupRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
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
        ProteinUpdateEvent.Handler,
        ProteinRequestEvent.Handler,
        GroupRequestEvent.Handler {



    public interface ThisView extends View {
        public void updateTitle(String title, String accession, String link);
        public void updateGeneGroupLink(String geneGroupId);
        public void updateUniquePeptideToProteinCount(int count);
        public void updateUniquePeptideToGeneCount(int count);
        public void updateNonUniquePeptidesCount(int count);
        public void updateName(String name);
        public void updateGeneSymbol(String geneSymbol);
        public void updateAlternativeName(String alternativeName);
        public void updateProteinEvidence(String proteinEvidence);
        public void updateDescription(String description);
        public void updateSpecies(String species);
        public void updateProperties(List<Pair<String, String>> links);
        public void clearProperties();
        public void clearTitle();
        public void displayLoadingMessage();

    }

    private boolean hiding = true;

    private static final String UNIPROTKB_URL = "http://www.uniprot.org/uniprot/";

    private boolean groupView;

    public HeaderPresenter(EventBus eventBus, ThisView view) {
        super(eventBus, view);

        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(GroupRequestEvent.getType(), this);
        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        groupView = event.getViewType() == ValidStateEvent.ViewType.Group;
        if (event.getViewType() == ValidStateEvent.ViewType.Protein ||
                event.getViewType() == ValidStateEvent.ViewType.Group) {
            hiding = false;
            getView().asWidget().setVisible(true);
        } else {
            hiding = true;
            getView().asWidget().setVisible(false);
        }
    }

    @Override
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        List<Group> groups = event.getGroups();

        if (groupView && groups.size() > 0) {
            getView().updateTitle("Protein group " + groups.get(0).getId(), groups.get(0).getId(), null);
            getView().updateDescription(groups.get(0).getDescription());
            List<Pair<String, String>> proteins = new ArrayList<>();

            for (String protID : groups.get(0).getMemberProteins()) {
                proteins.add(new Pair<>(protID, "protein=" + protID));
            }
            getView().updateProperties(proteins);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        List<Protein> proteins = event.getProteins();
        String gene = null;

        if (!hiding && event.getProteins().size() > 0) {

            if (!groupView) {
                final Protein protein = proteins.get(0);

                getView().clearTitle();
                getView().updateTitle(protein.getName() + " (" + protein.getAccession() + ")", protein.getAccession(), UNIPROTKB_URL);
                if(protein.getGenes()!= null && !protein.getGenes().isEmpty() && protein.getGenes().size()==1){
                    gene = protein.getGenes().get(0);
                }

                getView().updateGeneGroupLink(gene);
                getView().updateUniquePeptideToProteinCount(protein.getUniquePeptideToProteinCount());
                getView().updateUniquePeptideToGeneCount(protein.getUniquePeptideToGeneCount());
                getView().updateNonUniquePeptidesCount(protein.getNonUniquePeptidesCount());
                getView().updateName(protein.getName());
                getView().updateAlternativeName(protein.getAlternativeName());
                getView().updateSpecies(protein.getSpecies());
                getView().updateGeneSymbol(protein.getGeneSymbol());
                getView().updateProteinEvidence(protein.getProteinEvidence());

                //For groups
                getView().clearProperties();

            }
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        getView().displayLoadingMessage();
    }

    @Override
    public void onGroupRequestEvent(GroupRequestEvent event) {
        getView().displayLoadingMessage();
    }


}
