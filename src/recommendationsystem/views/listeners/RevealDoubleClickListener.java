package recommendationsystem.views.listeners;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import recommendationsystem.logging.MyLogger;
import recommendationsystem.views.RecommendationSystemView;

public class RevealDoubleClickListener implements IDoubleClickListener
{
    private RecommendationSystemView view;

    public RevealDoubleClickListener(RecommendationSystemView view)
    {
        this.view = view;
    }
    
    public void doubleClick(DoubleClickEvent event)
    {
        Object selection = ((StructuredSelection) event.getSelection())
                .getFirstElement();
        if (selection instanceof MethodDeclaration)
        {
            MyLogger.getInstance().logDoubleClickAction(
                    ((MethodDeclaration) selection).resolveBinding()
                            .getKey());
            view.reveal((ASTNode) selection);
        }
    }
}