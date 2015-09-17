package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
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
        public void updateTitle(String title, String accession, String link);
        public void updateUpGroupLink(String upGroupId);
        public void updateGeneGroupLink(String geneGroupId);
        public void updateUniquePeptideCount(int count);
        public void updateDescription(Description description);
        public void updateProperties(List<Pair<String, String>> links);
        public void clearProperties();
    }

    protected class Description {
        String accession = "";
        String altId = "";
        String name = "";
        String species = "";
        String geneSymbol = "";
        String proteinEvidence = "";
    }

    private static final String UNIPROT_URL = "http://www.uniprot.org/uniprot/";

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

        if (groupView && groups.size() > 0) {
            getView().updateTitle("Protein group " + groups.get(0).getId(), groups.get(0).getId(), null);
            Description proteinDescription = parseDescription(groups.get(0).getId(),groups.get(0).getDescription());

            getView().updateDescription(proteinDescription);
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

        if (!groupView) {
            Description proteinDescription = parseDescription(proteins.get(0).getAccession(), proteins.get(0).getDescription());
            getView().updateTitle(proteinDescription.geneSymbol + " (" + proteinDescription.accession + ")", proteinDescription.accession, UNIPROT_URL);
            getView().updateUpGroupLink(proteins.get(0).getAccession());
            getView().updateGeneGroupLink(proteins.get(0).getGene());
            getView().updateUniquePeptideCount(proteins.get(0).getUniquePeptideCount());
            getView().updateDescription(proteinDescription);
            getView().clearProperties();
        }
    }

    private Description parseDescription(String accession, String description) {
        // ToDo: this should be refactored to have individual fields, put the value parsing in the presenter or in the data model
        Description parsedDescription = new Description();

        String patternStr = "([A-Z_0-9]+)+\\s+(.+)\\s+OS=(.+)\\s+GN=([A-Z0-9_]+)(\\sPE=([1-5]).*)?";
        RegExp regExp = RegExp.compile(patternStr);
        MatchResult matcher = regExp.exec(description);
        boolean matchFound = (matcher != null);

        parsedDescription.accession = accession;
        parsedDescription.altId = description;// just in case the parsing fails
        if (matchFound) {
            String match = matcher.getGroup(1);
            if (match != null && !match.isEmpty()) {
                parsedDescription.altId = match;
                match = matcher.getGroup(2);
                if (match != null && !match.isEmpty()) {
                    parsedDescription.name = match;
                    match = matcher.getGroup(3);
                    if (match != null && !match.isEmpty()) {
                        parsedDescription.species = match;
                        match = matcher.getGroup(4);
                        if (match != null && !match.isEmpty()) {
                            parsedDescription.geneSymbol = match;
                            match = matcher.getGroup(6);
                            if (match != null && !match.isEmpty()) {
                                parsedDescription.proteinEvidence = match;
                            }
                        }
                    }
                }
            }
        }

        return parsedDescription;
    }
}
