package engine;

import java.util.concurrent.locks.ReentrantLock;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Class to create and manage a JavaScript engine.
 */
public class ScriptManager {

	/* The javax.script JavaScript engine used by this class. */
	private static ScriptEngine js_engine = new ScriptEngineManager().getEngineByName("JavaScript");
	/* The Invocable reference to the engine. */
	private static Invocable js_invocable = (Invocable) js_engine;
	
	private static ReentrantLock lock = new ReentrantLock(true);
	
	public static void lock()
	{
		lock.lock();
	}
	
	public static void unlock()
	{
		lock.unlock();
	}

	/**
	 * Used to bind the provided object to the name in the scope of the scripts
	 * being executed by this engine.
	 */
	public static void bindArgument(String name, Object obj) {
		//js_engine.put(name,obj);
		
		js_engine.put(name, obj);
	}
	
	public static void clearBindings()
	{
		js_engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
	}
	
	public static Object retrieveValue(String key)
	{
		return js_engine.get(key);
	}
	
	/**
	 * Will load the script source from the provided filename.
	 */
	public static void loadScript(String script_name) {
		try {
			js_engine.eval(new java.io.FileReader(script_name));
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(java.io.IOException iox) {
			iox.printStackTrace();
		}
	}
	
	public static void invokeFunction(String functionName) {
		try {
			js_invocable.invokeFunction(functionName);
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(NoSuchMethodException nsme) {
			nsme.printStackTrace();
		}
	}
	
	public static void invokeFunction(String functionName, Object... args) {
		try {
			js_invocable.invokeFunction(functionName, args);
		}
		catch(ScriptException se) {
			se.printStackTrace();
		}
		catch(NoSuchMethodException nsme) {
			nsme.printStackTrace();
		}
	}
}

