package jp.aegif.nemaki.util.spring.aspect.log;

import javax.annotation.PostConstruct;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import jp.aegif.nemaki.util.spring.aspect.log.impl.CustomToStringImpl;

public class DefaultLogger {

	private static Logger log = Logger.getLogger(DefaultLogger.class);
	private String logLevel;
	private boolean returnValue;
	private boolean fullQualifiedName;
	private boolean arguments;
	private boolean beforeEnabled;
	private boolean afterEnabled;
	private boolean callContextEnabled;
	
	private CustomToStringImpl customToString;

   @PostConstruct
	public void init() {
		if (log != null){
			log.setLevel(Level.toLevel(logLevel));
		}
	}

	public Object aroundMethod(ProceedingJoinPoint jp) throws Throwable{

		StringBuilder sb = new StringBuilder();
		Object[] args = jp.getArgs();

		//Parse callContext
		if(callContextEnabled){
			CallContext callContext = getCallContext(args);
			if(callContext == null){
				sb.append("N/A; ");
			}else{
				String userId = callContext.getUsername();
				sb.append("UserId=" + userId + " executes; ");
			}
		}

		//Method name
		if(fullQualifiedName){
			sb.append(jp.getTarget().getClass().getName());
		}else{
			sb.append(jp.getTarget().getClass().getSimpleName());
		}
		sb.append("#").append(jp.getSignature().getName());
		if(arguments){
			sb.append(customToString.parseArguments(args));
		}

		//Before advice
		if(beforeEnabled){
			log.info("nemaki_log[BEFORE]; " + sb.append("; ").toString());
		}

		//Execute method
		try{
			Object result = jp.proceed();

			//After advice
			if(afterEnabled){
				sb.append("returned ");
				if (returnValue && result != null){
					String resultString = customToString.parse(result);
					sb.append(resultString);
				}				
					
				log.info("nemaki_log[AFTER]; " + sb.append("; ").toString());
			}

			return result;
		}catch(Exception e){
			log.error("nemaki_log[ERROR];" + e.toString(), e);
			throw e;
		}
	}

	private CallContext getCallContext(Object[] args){
		if(args != null && args.length > 0){
			for(int i=0; i < args.length; i++){
				Object arg = args[i];
				if(arg != null && arg instanceof CallContext){
					CallContext callContext = (CallContext)arg;
					return callContext;
				}
			}
		}
		return null;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public void setReturnValue(boolean returnValue) {
		this.returnValue = returnValue;
	}

	public void setFullQualifiedName(boolean fullQualifiedName) {
		this.fullQualifiedName = fullQualifiedName;
	}

	public void setArguments(boolean arguments) {
		this.arguments = arguments;
	}

	public void setBeforeEnabled(boolean beforeEnabled) {
		this.beforeEnabled = beforeEnabled;
	}

	public void setAfterEnabled(boolean afterEnabled) {
		this.afterEnabled = afterEnabled;
	}

	public void setCallContextEnabled(boolean callContextEnabled) {
		this.callContextEnabled = callContextEnabled;
	}

	public void setCustomToString(CustomToStringImpl customToString) {
		this.customToString = customToString;
	}
	
}
