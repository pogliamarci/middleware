package it.polimi.jmsgrid.worker;

import javax.jms.Destination;

/**
 * A ClassLoader which asks the JMS destination specified using the setDestination()
 * method to send via a JMS message the bytecode of the loaded classes, if not found
 * locally by the parent classloader.
 *
 */
public class CodeOnDemandClassLoader extends ClassLoader {
	
	private CommunicationHandler handler;
	private Destination queue;
	
	public CodeOnDemandClassLoader(CommunicationHandler handler, Destination queue) {
		super(CodeOnDemandClassLoader.class.getClassLoader());	
		this.handler = handler;
		this.queue = queue;
	}
	
	@Override
	public Class<?> loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
		
		Class<?> result = loadClass(className);
		
		/* Load reference classes as well if required */
		if (resolveIt) {
			resolveClass(result);
		}
		
		return result;
	}
	
	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		return loadClass(className);
	}
	
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		/* Call super class loader */
		try {
			return super.findSystemClass(className); 
		} catch (ClassNotFoundException e) { 
			return loadClassImpl(className);
		}
	}
	
	private Class<?> loadClassImpl(String className) throws ClassNotFoundException {
		/* Ask class to client */
		byte classData[] = handler.lookupClass(className, queue);
		if (classData == null) {
			throw new ClassNotFoundException();
		}

		/* Define the class */
		Class<?> result = defineClass(className, classData, 0, classData.length, null);
		if (result == null) {
			throw new ClassFormatError();
		}
		return result;  
	}

	public void setDestination(Destination destination) {
		this.queue = destination;
	}
}
