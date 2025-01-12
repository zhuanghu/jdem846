package us.wthr.jdem846.scripting;

import us.wthr.jdem846.exception.ScriptCompilationFailedException;
import us.wthr.jdem846.exception.ScriptingException;
import us.wthr.jdem846.logging.Log;
import us.wthr.jdem846.logging.Logging;
import us.wthr.jdem846.scripting.groovy.GroovyScriptLoader;
import us.wthr.jdem846.scripting.javascript.JavaScriptLoader;
import us.wthr.jdem846.scripting.scala.ScalaScriptLoader;

public class ScriptProxyFactory
{
	@SuppressWarnings("unused")
	private static Log log = Logging.getLog(ScriptProxyFactory.class);
	
	public static ScriptProxy createScriptProxy(ScriptLanguageEnum language, String scriptContent) throws ScriptingException, ScriptCompilationFailedException
	{
		ScriptProxy scriptProxy = null;
		
		if (language == ScriptLanguageEnum.GROOVY) {
			scriptProxy = GroovyScriptLoader.parseScript(scriptContent);
		} else if (language == ScriptLanguageEnum.SCALA) {
			scriptProxy = ScalaScriptLoader.parseScript(scriptContent);
		} else if (language == ScriptLanguageEnum.JAVASCRIPT) {
			scriptProxy = JavaScriptLoader.parseScript(scriptContent);
		} /*else if (language == ScriptLanguageEnum.JYTHON) {
			
		}*/ else {
			throw new ScriptingException("Unsupported language: " + language.text());
		}
		
		return scriptProxy;
	}
	
}
