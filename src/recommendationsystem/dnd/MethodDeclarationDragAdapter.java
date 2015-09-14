package recommendationsystem.dnd;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.widgets.Table;

/**
 * MethodDeclarationDragAdapter enables drag support for the MethodDeclaration
 * data that is stored in the current method, recommendations and pinned methods
 * tables. To prevent having to serialize the MethodDeclaration class, only the
 * method's key itself is passed as a string during the drag operation.
 * <p>
 * The adapter only transfers a single item. If multiple are selected, the
 * adapter only considers the first selection to be selected.
 * 
 * @author David Piorkowski
 * 
 */
public class MethodDeclarationDragAdapter extends DragSourceAdapter
{
    private StructuredViewer viewer;

    /**
     * The constructor. A pointer to the Viewer must be passed is so we can
     * determine which items have been selected. This also allows us to reuse
     * this adapter for both the current method table and the recommendations
     * table.
     * 
     * @param viewer
     *            The StructuredViewer (or TableViewer) where the drag starts.
     */
    public MethodDeclarationDragAdapter(StructuredViewer viewer)
    {
        this.viewer = viewer;
    }

    /**
     * Sets the data to be passed along during the drag operation. In this case,
     * it's going to be the method's key.
     */
    @Override
    public void dragSetData(DragSourceEvent event)
    {
        StringBuilder sb = new StringBuilder();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        sb.append(((MethodDeclaration) selection.getFirstElement())
                .resolveBinding().getKey());
        sb.append('\t');

        TableViewer tv = (TableViewer) viewer;
        Table t = tv.getTable();
        if (t.getItemCount() > 1)
        {
            String[] cues = (String[]) t.getItem(t.getSelectionIndex() + 1)
                    .getData();
            for (String cue : cues)
            {
                sb.append(cue);
                sb.append('\t');
            }
        }

        event.data = sb.toString();
        // System.out.println("Drag sent: " + event.data);
    }

    /**
     * Restricts drag operations to table rows that have MethodDeclarations in
     * them. This prevents us from dragging the array of cues.
     */
    @Override
    public void dragStart(DragSourceEvent event)
    {
        event.doit = !viewer.getSelection().isEmpty()
                && ((IStructuredSelection) viewer.getSelection())
                        .getFirstElement() instanceof MethodDeclaration;
    }
}
