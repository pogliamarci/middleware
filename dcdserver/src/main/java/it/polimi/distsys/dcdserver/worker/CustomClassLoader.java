package it.polimi.distsys.dcdserver.worker;

public class CustomClassLoader extends ClassLoader {
	
	public CustomClassLoader() {
		super(CustomClassLoader.class.getClassLoader());
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
		
		/* Call super class loader */
		try {
			result = super.findSystemClass(className); 
			return result;
		} catch (ClassNotFoundException e) {
			System.err.println("WARNING: Not a class parent classloader can load.");  
		}

		System.err.println("Attempting to load class "+className+" from client...");
		
		/* Try to load it from our repository */
		classData = getClassFromClient(className);
		if (classData == null) {
			throw new ClassNotFoundException();
		}
		
		System.err.println("Class "+className+" loaded.");
		
		/* Define the class */
		result = defineClass(className, classData, 0, classData.length);
		if (result == null) {
			System.out.println("Format Error");
			throw new ClassFormatError();
		}

		return result;  
	}

	private byte[] getClassFromClient(String className) {
		// TODO a great question mark...
		return null;
	}
}
