package com.upcrob.jotp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upcrob.jotp.controllers.EmailOtpController;
import com.upcrob.jotp.controllers.MonitorController;
import com.upcrob.jotp.controllers.OtpValidationController;
import com.upcrob.jotp.controllers.TextOtpController;

/**
 * Servlet implementation class ControllerServlet
 */
@WebServlet(urlPatterns={"/*"}, loadOnStartup=1)
public class ControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String CONFIG_PATH_PROPERTY = "com.upcrob.jotp.config.dir";
	
    private Map<String, Controller> getControllers;
    private Map<String, Controller> postControllers;
    private Configuration config;
	private Tokenstore tokenstore;
	private Reaper reaper;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ControllerServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Controller controller = getControllers.get(request.getPathInfo());
		if (controller != null) {
			// Add request parameters to map
			Map<String, String> paramMap = new HashMap<String, String>();
			Enumeration<String> paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();
				paramMap.put(name, request.getParameter(name));
			}
			
			// Execute request
			Response resp = controller.execute(paramMap);
			PrintWriter out = response.getWriter();
			out.write(resp.toString());
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Controller controller = postControllers.get(request.getPathInfo());
		if (controller != null) {
			// Add request parameters to map
			Map<String, String> paramMap = new HashMap<String, String>();
			Enumeration<String> paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();
				paramMap.put(name, request.getParameter(name));
			}
						
			// Execute request
			Response resp = controller.execute(paramMap);
			PrintWriter out = response.getWriter();
			out.write(resp.toString());
			out.close();
		}
	}

	@Override
	public void init() throws ServletException {
		Logger log = LoggerFactory.getLogger(ControllerServlet.class);
		log.info("Initializing servlet...");
        
		// Set config path
		String configDir = System.getProperty(CONFIG_PATH_PROPERTY);
		if (configDir == null) {
			// Default to <USER HOME>/.jotp
			configDir = System.getProperty("user.home") + "/.jotp";
		} else {
			// Remove '/' character if it was added to the directory path
			if (configDir.endsWith("/")) {
				configDir = configDir.substring(0, configDir.length() - 1);
			}
		}
		
        // Load config
        String configPath = configDir + "/config.yaml";
        log.info("Attempting to load configuration from file: " + configPath);
        try {
			config = new YamlConfiguration(configPath);
		} catch (ConfigurationException e) {
			log.error("An error occurred while loading configuration: " + e.getMessage());
			throw new ServletException("An error occurred during configuration.  See the jOTP log for details.");
		} catch (FileNotFoundException e) {
			log.error("Configuration file not found: " + configPath);
			throw new ServletException("An error occurred during configuration.  See the jOTP log for details.");
		}
        log.info("Configuration loaded successfully.");
        
        // Setup Tokenstore
        TokenstoreType type = config.getTokenstoreType();
        switch (type) {
        	case LOCAL:
        		// Use a local, in-memory tokenstore
        		tokenstore = new LocalTokenstore(config);
        		break;
        	case JDBC:
        		// Use a JDBC datasource for the tokenstore
        		tokenstore = new JdbcTokenstore(config);
        		break;
        	case REDIS:
        		// Use a Redis server for the tokenstore
        		tokenstore = new RedisTokenstore(config);
        		break;
        }
        
        // Map controllers to URLs
        MonitorController mc = new MonitorController();
        getControllers = new HashMap<String, Controller>();
        getControllers.put("/sys/monitor", mc);
        
        postControllers = new HashMap<String, Controller>();
        postControllers.put("/sys/monitor", mc);
        postControllers.put("/otp/text", new TextOtpController(config, tokenstore));
        postControllers.put("/otp/email", new EmailOtpController(config, tokenstore));
        postControllers.put("/otp/validate", new OtpValidationController(config, tokenstore));
        
        // Start reaper thread
        reaper = new Reaper(tokenstore);
        reaper.start();
	}
}
