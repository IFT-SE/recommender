package recommendationsystem.providers;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import recommendationsystem.controllers.DbManager;
import recommendationsystem.controllers.ModelManager;
import recommendationsystem.model.CurrentRecommendations;
import recommendationsystem.model.MethodHistory;

/**
 * RecommendationsContentProvider populates the data fields of the pinned method
 * viewer located at the middle of the plug-in. It calls on
 * {@link CurrentRecommendations} to determine the current recommended methods
 * and their cues before sending it out to the table.
 * <p>
 * The currently recommended methods list is not stored here, but in
 * CurrentRecommendations. The expectation is that inputChanged will be called
 * once, when setInput() is called on the viewer, and from then on any change of
 * state is stored in the classes under the model package.
 * 
 * @author David Piorkowski
 * 
 */
public class RecommendationsContentProvider implements
        IStructuredContentProvider
{
    CurrentRecommendations currentRecommendations;
    DbManager dbManager;
    MethodHistory methodHistory;
    ModelManager modelManager;

    /**
     * Does nothing.
     */
    @Override
    public void dispose()
    {
    }

    /**
     * Initializes the {@link DbManager}, {@link MethodHistory},
     * {@link ModelManager} and sets CurrentRecommendations as the input source.
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        currentRecommendations = (CurrentRecommendations) newInput;
        dbManager = DbManager.getInstance();
        methodHistory = MethodHistory.getInstance();
        modelManager = ModelManager.getInstance();
    }

    /**
     * Depending on whether words are enabled or not, the Object array contains
     * two different possibilities. If words are enabled, the array contains a
     * {@link MethodDeclaration} node followed by a String[] of cues for that
     * node and repeats. If words are disabled, the Object array contains only
     * MethodDeclaration nodes.
     * <p>
     * All data is gathered from the {@link CurrentRecommendations} which is
     * used to store the state of the list of currently recommended methods.
     * <p>
     * Returns:
     * <ul>
     * <li>Object [ MethodDeclaration, String[], MethodDeclaration, String[] ...
     * ] if words are enabled
     * <li>Object [ MethodDeclaration, MethodDeclaration, ... ] if words are not
     * enabled
     * </ul>
     */
    @Override
    public Object[] getElements(Object inputElement)
    {
        MethodDeclaration[] currentRecs = currentRecommendations
                .getCurrentRecommendations();
        String[][] allCues = currentRecommendations.getCues();

        Object[] rv;

        if (modelManager.getWordsEnabled())
        {

            rv = new Object[2 * currentRecs.length];

            for (int i = 0; i < currentRecs.length; i++)
            {
                MethodDeclaration method = currentRecs[i];
                String[] cues = allCues[i];
                rv[2 * i] = method;
                rv[2 * i + 1] = cues;
            }
        }
        else
        {
            rv = new Object[currentRecs.length];
            for (int i = 0; i < currentRecs.length; i++)
            {
                MethodDeclaration method = currentRecs[i];
                rv[i] = method;
            }
        }

        return rv;
    }

}
