package recommendationsystem.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class PluginStyles
{
    private static PluginStyles instance;
    private Color visitedMethodBgColor;
    private Color unvisitedMethodBgColor;
    private Font methodFont;
    private final Display disp = Display.getCurrent();

    private PluginStyles()
    {

    }

    public synchronized static PluginStyles getInstance()
    {
        if (instance == null)
        {
            instance = new PluginStyles();
            instance.init();
        }
        return instance;
    }

    /**
     * MethodManager is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    public enum ColorList
    {
        VISITED_METHOD_BG, UNVISITED_METHOD_BG;
    }

    private void init()
    {
        visitedMethodBgColor = new Color(disp, 240, 240, 240);
        unvisitedMethodBgColor = disp.getSystemColor(SWT.COLOR_WHITE);
        methodFont = new Font(disp, "Segoe UI", 11, SWT.NONE);
    }

    public Color getColor(ColorList color)
    {
        switch (color)
        {
            case UNVISITED_METHOD_BG:
                return unvisitedMethodBgColor;
            case VISITED_METHOD_BG:
                return visitedMethodBgColor;
        }
        return null;
    }

    public Font getMethodFont()
    {
        return methodFont;
    }

    public void dispose()
    {
        if (!visitedMethodBgColor.isDisposed())
            visitedMethodBgColor.dispose();
        if (!methodFont.isDisposed())
            methodFont.dispose();
        instance = null;
    }
}
