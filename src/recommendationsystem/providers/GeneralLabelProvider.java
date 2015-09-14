package recommendationsystem.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;

/**
 * GeneralLabelProvider provides methods that are shared between all three
 * tables in the plug-in. This class is further extended by
 * {@link RecommendationsLabelProvider}.
 * 
 * @author David Piorkowski
 * 
 */
public class GeneralLabelProvider extends GeneralColumnLabelProvider
{
    @Override
    public String getText(Object element)
    {
        return makePretty((MethodDeclaration) element);
    }

    @Override
    public Image getImage(Object element)
    {
        if (element instanceof MethodDeclaration)
        {
            MethodDeclaration method = (MethodDeclaration) element;
            int flags = method.getModifiers();

            if (Modifier.isPrivate(flags))
                return JavaUI.getSharedImages().getImage(
                        ISharedImages.IMG_OBJS_PRIVATE);
            else if (Modifier.isProtected(flags))
                return JavaUI.getSharedImages().getImage(
                        ISharedImages.IMG_OBJS_PROTECTED);

            return JavaUI.getSharedImages().getImage(
                    ISharedImages.IMG_OBJS_PUBLIC);
        }
        return null;
    }

    public String getToolTipText(Object element)
    {
        if (element instanceof MethodDeclaration)
        {
            String newLine = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            MethodDeclaration method = (MethodDeclaration) element;

            if (method.getJavadoc() == null)
                sb.append("(No Javadoc available)");
            else
            {
                sb.append("Javadoc:");
                sb.append(newLine);
                sb.append(((MethodDeclaration) element).getJavadoc().toString());
            }
            return sb.toString();
        }
        return null;
    }

    protected String makePretty(MethodDeclaration method)
    {
        StringBuilder rv = new StringBuilder();
        SingleVariableDeclaration var;
        rv.append(method.getName().toString());
        rv.append(" (");
        for (Object param : method.parameters())
        {
            var = (SingleVariableDeclaration) param;
            rv.append(var.toString());
            rv.append(',');
        }
        if (method.parameters().size() > 0)
            rv.deleteCharAt(rv.length() - 1);
        if (method.getReturnType2() != null)
        {
            rv.append(") : ");
            rv.append(method.getReturnType2().toString());
        }

        rv.append(" - ");
        CompilationUnit unit = (CompilationUnit) method.getRoot();
        IJavaElement javaElem = unit.getJavaElement();
        rv.append(javaElem.getElementName());

        return rv.toString();
    }
}
