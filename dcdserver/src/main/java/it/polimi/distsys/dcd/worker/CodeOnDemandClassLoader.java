package it.polimi.distsys.dcd.worker;

import javax.jms.Destination;

public class CodeOnDemandClassLoader extends ClassLoader {
	
	private CommunicationHandler handler;
	private Destination queue;
	
	public CodeOnDemandClassLoader(CommunicationHandler handler, Destination queue) {
		super(CodeOnDemandClassLoader.class.getClassLoader());

		System.out.println("ClassLoader CREATED [" + queue + "]");
		
		this.handler = handler;
		this.queue = queue;
	}
	
	@Override
	public synchronized Class<?> loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
		
		Class<?> result = loadClass(className);
		
		/* Load reference classes as well if required */
		if (resolveIt) {
			resolveClass(result);
		}
		
		return result;
	}
	
	@Override
	public synchronized Class<?> findClass(String className) throws ClassNotFoundException {
		return loadClass(className);
	}
	
	// Main function called by client to resolve class
	@Override
	public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {

		Class<?> result;
		byte classData[];
		
		System.out.println("Load Class CALLED with " + className + " as a parameter. [" + queue + "]");
		
		/* Call super class loader */
		try {
			return super.findSystemClass(className); 
		} catch (ClassNotFoundException e) {
			System.err.println("WARNING: Not a class parent classloader can load.");  
		}

		System.err.println("Attempting to load class "+className+" from client...");
		
		/* Try to load it from our repository */
		classData = getClassFromClient(className);
		if (classData == null) {
			System.out.println("Class data is NULL");
			throw new ClassNotFoundException();
		}
		
		System.err.println("Class "+className+" loaded.");
		
		/* Define the class */
		result = defineClass(className, classData, 0, classData.length, null);
		if (result == null) {
			System.err.println("Format Error");
			throw new ClassFormatError();
		}
		
		return result;  
	}

	private byte[] getClassFromClient(String className) {
		System.out.println("Requesting to client class " + className);
		return handler.lookupClass(className, queue);
	}

	public void setDestination(Destination destination) {
		this.queue = destination;
	}
}
