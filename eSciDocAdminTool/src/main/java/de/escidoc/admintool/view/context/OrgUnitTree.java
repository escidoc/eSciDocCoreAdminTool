package de.escidoc.admintool.view.context;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.themes.Reindeer;

import de.escidoc.admintool.service.OrgUnitService;
import de.escidoc.admintool.view.OrgUnitTreeFactory;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.oum.OrganizationalUnit;

public class OrgUnitTree extends CustomComponent {
    private static final long serialVersionUID = 671280566874568107L;

    private final Logger log = LoggerFactory.getLogger(OrgUnitTree.class);

    private Tree tree = new Tree();

    private final OrgUnitService service;

    private OrgUnitTreeFactory orgUnitTreeFactory;

    public OrgUnitTree(final OrgUnitService service) {
        this.service = service;
        final Panel panel = new Panel();
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        tree.setHeight(null);
        tree.setWidth("100%");
        tree.setMultiSelect(true);
        tree.setImmediate(true);
        panel.addComponent(tree);
        setCompositionRoot(panel);
        loadOrgUnits();
    }

    public void setMultiSelect(final boolean multiSelect) {
        tree.setMultiSelect(multiSelect);
    }

    /**
     * Replace me by a call of the service.
     */
    private void loadOrgUnits() {
        try {
            tree.removeAllItems();

            orgUnitTreeFactory =
                new OrgUnitTreeFactory(tree, new ArrayList<OrganizationalUnit>(
                    service.getOrganizationalUnits()), service.getOrgUnitById());
            tree = orgUnitTreeFactory.create();

        }
        catch (final InternalClientException e) {
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();

        }
        catch (final TransportException e) {
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();

        }
        catch (final EscidocException e) {
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
    }

    private void loadFakeTreeData() {
        String rootOrg = "External Organizations";
        tree.addItem(rootOrg);

        Object[] extOrg =
            new Object[] { "Angelo State University",
                "Berlin University of the Arts", "Bond University",
                "Brandeis University", "Bryn Mawr College",
                "Cardiff University", "..." };

        for (final Object o : extOrg) {
            tree.addItem(o);
            // Set it to be a child.
            tree.setParent(o, rootOrg);
            // Make the moons look like leaves.
            tree.setChildrenAllowed(o, false);
        }

        rootOrg = "Kaiser-Wilhelm-Gesellschaft";
        tree.addItem(rootOrg);
        final String child =
            "Kaiser Wilhelm Institut für Züchtungsforschung (geschlossen)";
        tree.addItem(child);
        // Set it to be a child.
        tree.setParent(child, rootOrg);
        // Make the moons look like leaves.
        tree.setChildrenAllowed(child, false);

        rootOrg = "Max Planck Society";
        tree.addItem(rootOrg);

        extOrg =
            new Object[] { "Fritz Haber Institute",
                "Max Planck Digital Library", "MPI for Astrophysics",
                "MPI for biophysical chemistry", };

        for (final Object o : extOrg) {
            tree.addItem(o);
            // Set it to be a child.
            tree.setParent(o, rootOrg);
            // Make the moons look like leaves.
            tree.setChildrenAllowed(o, false);
        }
        final Object subParent = "MPI for Chemical Ecology";
        tree.addItem(subParent);
        tree.setParent(subParent, rootOrg);
        tree.setChildrenAllowed(subParent, true);
        final String subUnit =
            "Department of Biochemistry, Prof. J. Gershenzon";
        tree.addItem(subUnit);
        // Set it to be a child.
        tree.setParent(subUnit, subParent);
        // Make the moons look like leaves.
        tree.setChildrenAllowed(subUnit, true);

        final Object[] subsubUnit =
            new Object[] {
                "Research Group Dr. G. Kunert, Chemical Communication in Plant-Aphid-Interactions",
                "Research Group Dr. J. D'Auria, Biochemistry and Evolution of Tropane Alkaloid Biosynthesis",
                "Research Group Dr. S. Unsicker, Chemical Ecology of Trees" };

        for (final Object o : subsubUnit) {
            tree.addItem(o);
            // Set it to be a child.
            tree.setParent(o, subUnit);
            // Make the moons look like leaves.
            tree.setChildrenAllowed(o, false);
        }

        final Object[] restUnit =
            new Object[] {
                "Department of Bioorganic Chemistry, Prof. Dr. W. Boland",
                "Department of Entomology, Prof. D. G. Heckel",
                "Department of Evolutionary Neuroethology, Prof. B. S. Hansson",
                "Department of Genetics and Evolution",
                "Department of Molecular Ecology, Prof. I. T. Baldwin",
                "IMPRS on Ecological Interactions",
                "Max Planck Research Group Insect Symbiosis",
                "Research Group Biosynthesis / NMR",
                "Research Group Mass Spectrometry", "..." };

        for (final Object o : restUnit) {
            tree.addItem(o);
            // Set it to be a child.
            tree.setParent(o, subParent);
            // Make the moons look like leaves.
            tree.setChildrenAllowed(o, false);
        }

        rootOrg = "物質・材料研究機構/ National Institute for Materials Science";
        tree.addItem(rootOrg);

        final Object[] asia =
            new Object[] { "総務部 / General Affairs Division",
                "人事課 / Personnel Section", "契約課 / Contract Section", "..."

            };

        for (final Object o : asia) {
            tree.addItem(o);
            // Set it to be a child.
            tree.setParent(o, rootOrg);
            // Make the moons look like leaves.
            tree.setChildrenAllowed(o, false);
        }
    }

    public Object getSelectedItems() {
        return tree.getValue();
    }
}