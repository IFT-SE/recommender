package recommendationsystem.dnd;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import recommendationsystem.model.PinnedMethodList;

/**
 * MethodDeclarationDropAdapter enables drop support for the MethodDeclaration
 * data that is stored in the current method, recommendations and pinned methods
 * tables. To prevent having to serialize the MethodDeclaration class, only the
 * method's key itself is passed as a string during the drag operation.
 * <p>
 * The adapter only transfers a single item. If multiple are selected, the
 * adapter only considers the first selection to be selected.
 * <p>
 * This dropAdapter (unfortunately) has code specific to the PinnedMethodList
 * and should not be used elsewhere.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodDeclarationDropAdapter extends ViewerDropAdapter
{
    private PinnedMethodList pinnedMethods;

    /**
     * The constructor. A pointer to the Viewer must be passed in for the
     * parent's constructor.
     * 
     * @param viewer
     *            The StructuredViewer (or TableViewer) where the drag ends.
     */
    public MethodDeclarationDropAdapter(TableViewer viewer)
    {
        super(viewer);
        pinnedMethods = PinnedMethodList.getInstance();
    }

    /**
     * Executes the drop and refreshes the pinned methods tables. Invalid text
     * strings are handled in the PinnedMethodsList object. We expect the data
     * to be a method's key.
     */
    @Override
    public boolean performDrop(Object data)
    {
        if (data instanceof String)
        {
            String[] dataArray = ((String) data).trim().split("\t");
            if (dataArray.length > 0)
            {
                String methodKey = dataArray[0];
                String[] cues = new String[dataArray.length - 1];
                for (int i = 1; i < dataArray.length; i++)
                {
                    cues[i - 1] = dataArray[i];
                }
                // System.out.println("Drop received: " + data.toString());
                pinnedMethods.addPinnedMethod(methodKey, cues);
                if (pinnedMethods.getLength() > 0
                        && !((TableViewer) getViewer()).getTable().isDisposed())
                {
                    getViewer().refresh();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Validates dropping on the given object. This method is called whenever
     * some aspect of the drop operation changes. This always returns true. We
     * handle validation in the model, specifically in the PinnedMethodsList.
     */
    @Override
    public boolean validateDrop(Object target, int op, TransferData type)
    {
        // TODO: Determine if there is need for validation, possibly to get the
        // "don't" sign
        return true;
    }
}
