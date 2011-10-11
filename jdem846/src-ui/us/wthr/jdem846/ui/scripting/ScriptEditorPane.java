package us.wthr.jdem846.ui.scripting;

import java.io.IOException;
import java.net.URL;

import javax.swing.text.EditorKit;

import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.ui.base.EditorPane;

/** A _very_ basic script editor. I really don't feel like writing this, so it's very 
 * possible this will be replaced with a 3rd party component.
 * 
 * @author Kevin M. Gill
 *
 */
@SuppressWarnings("serial")
public class ScriptEditorPane extends EditorPane
{
	private static Log log = Logging.getLog(ScriptEditorPane.class);

	public ScriptEditorPane()
	{
		super();
	}

	public ScriptEditorPane(String type, String text)
	{
		super(type, text);
	}

	public ScriptEditorPane(String url) throws IOException
	{
		super(url);
	}

	public ScriptEditorPane(URL initialPage) throws IOException
	{
		super(initialPage);
	}


	
	
}
