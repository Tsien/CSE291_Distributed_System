package rmi;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class IsRemoteInterface {
    /**
     * check whether <code>c</code> does not represent a remote interface -
     * an interface whose methods are all marked as throwing
     * <code>RMIException</code>.
     * @param c
     * @return
     */
	public static boolean check(Class<?> c) {
    	if (!c.isInterface()) 
    		return false;
    	Method[] methods = c.getMethods();
    	for (Method m : methods) {
    		Class<?>[] exps = m.getExceptionTypes();
//    		System.out.println("Class is:" + exps[0].toString());
    		if (!Arrays.asList(exps).contains(RMIException.class)) {
    			return false;
    		}
    	}
		return true;
    }
}
