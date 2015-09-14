package recommendationsystem.providers;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import recommendationsystem.views.PluginStyles;

public class GeneralColumnLabelProvider extends ColumnLabelProvider
{

    @Override
    public void update(ViewerCell cell)
    {
        super.update(cell);

        if (getImage(cell.getElement()) != null)
        {
            cell.setFont(PluginStyles.getInstance().getMethodFont());
        }
    }
}
