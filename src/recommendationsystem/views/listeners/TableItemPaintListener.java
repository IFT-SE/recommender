package recommendationsystem.views.listeners;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class TableItemPaintListener implements Listener
{
    @Override
    public void handleEvent(Event event)
    {
        Display disp = Display.getCurrent();
        TextLayout textLayout = new TextLayout(disp);
        Font font = new Font(disp, "Arial", 10, SWT.ITALIC);
        TextStyle textStyle = new TextStyle(font,
                disp.getSystemColor(SWT.COLOR_DARK_GRAY), null);
        // TODO Auto-generated method stub
        textLayout.setStyle(textStyle, 0, 100);
        textLayout.draw(event.gc, event.x, event.y);

    }

}
