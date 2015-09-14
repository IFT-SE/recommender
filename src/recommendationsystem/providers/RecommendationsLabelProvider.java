package recommendationsystem.providers;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class RecommendationsLabelProvider extends GeneralLabelProvider
{

    public RecommendationsLabelProvider()
    {

    }

    @Override
    public String getText(Object element)
    {
        if (element instanceof MethodDeclaration)
            return makePretty((MethodDeclaration) element);
        // return ((MethodDeclaration) element).getName().toString();
        else
        {
            StringBuilder sb = new StringBuilder();
            String[] cues = (String[]) element;
            for (String cue : cues)
            {
                if (cue != null)
                {
                    sb.append(cue);
                    sb.append(' ');
                }
            }
            return sb.toString();
        }
    }
   
}
