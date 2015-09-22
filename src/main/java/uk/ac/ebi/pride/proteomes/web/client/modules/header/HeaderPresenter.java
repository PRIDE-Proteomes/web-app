package uk.ac.ebi.pride.proteomes.web.client.modules.header;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
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
        ProteinRequestEvent.Handler {

    public interface ThisView extends View {
        public void updateTitle(String title, String accession, String link);

        public void updateUpGroupLink(String upGroupId);

        public void updateGeneGroupLink(String geneGroupId);

        public void updateUniquePeptideToProteinCount(int count);

        public void updateUniquePeptideToIsoformCount(int count);

        public void updateUniquePeptideToGeneCount(int count);

        public void updateNonUniquePeptidesCount(int count);

        public void updateDescription(Description description);

        public void updateProperties(List<Pair<String, String>> links);

        public void clearProperties();

        public void clearTitle();

        public void displayLoadingMessage();

    }

    private boolean hiding = true;

    protected class Description {
        String accession = "";
        String altId = "";
        String name = "";
        String species = "";
        String geneSymbol = "";
        String proteinEvidence = "";
    }

    private static final String UNIPROTKB_URL = "http://www.uniprot.org/uniprot/";

    private boolean groupView;

    public HeaderPresenter(EventBus eventBus, ThisView view) {
        super(eventBus, view);

        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
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
            Description proteinDescription = parseDescription(groups.get(0).getId(), groups.get(0).getDescription());

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
        if (!hiding && event.getProteins().size() > 0) {

            if (!groupView) {
                Description proteinDescription = parseDescription(proteins.get(0).getAccession(), proteins.get(0).getDescription());
                getView().clearTitle();
                getView().updateTitle(proteinDescription.geneSymbol + " (" + proteinDescription.accession + ")", proteinDescription.accession, UNIPROTKB_URL);
                getView().updateUpGroupLink(proteins.get(0).getAccession());
                getView().updateGeneGroupLink(proteins.get(0).getGene());
                getView().updateUniquePeptideToProteinCount(proteins.get(0).getUniquePeptideToProteinCount());
                getView().updateUniquePeptideToIsoformCount(proteins.get(0).getUniquePeptideToIsoformCount());
                getView().updateUniquePeptideToGeneCount(proteins.get(0).getUniquePeptideToGeneCount());
                getView().updateNonUniquePeptidesCount(proteins.get(0).getNonUniquePeptidesCount());
                getView().updateDescription(proteinDescription);
                //For groups
                getView().clearProperties();

            }
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        getView().displayLoadingMessage();
    }


    private Description parseDescription(String accession, String description) {
        // ToDo: this should be refactored to have individual fields, put the value parsing in the presenter or in the data model
        Description parsedDescription = new Description();

        String patternStr = "([A-Z_0-9]+)+\\s+(.+)\\s+OS=(.+)\\s+GN=([A-Za-z0-9_]+)(\\sPE=([1-5]).*)?";
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
                                parsedDescription.proteinEvidence = proteinEvidenceDescription(match);

                            }
                        }
                    }
                }
            }
        }

        return parsedDescription;
    }

    private String proteinEvidenceDescription(String match) {
        //TODO This information should be parse in the web service or stored properly in the DB.

        String proteinEvidence = match;
        //Try to substitute the right description by value
        try {
            int pe = Integer.parseInt(match);
            switch (pe) {
                case 1:
                    proteinEvidence = "Protein Level";
                    break;
                case 2:
                    proteinEvidence = "Transcript Level";
                    break;
                case 3:
                    proteinEvidence = "Inferred from Homology";
                    break;
                case 4:
                    proteinEvidence = "Predicted";
                    break;
                case 5:
                    proteinEvidence = "Uncertain";
                    break;
                default:
                    //We return the unparsed String
            }
        } catch (NumberFormatException e) {
            //We return the unparsed String
        }

        return proteinEvidence;
    }
}
