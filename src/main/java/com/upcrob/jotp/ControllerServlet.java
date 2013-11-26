package com.upcrob.jotp;

import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(urlPatterns={"/otp/*"}, loadOnStartup=1)
public class ControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Map<String, Controller> getControllers;
    private Map<String, Controller> postControllers;
    private Configuration config;
	private Model model;
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
			PrintWriter out = response.getWriter();
			out.write(controller.execute(request));
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Controller controller = postControllers.get(request.getPathInfo());
		if (controller != null) {
			PrintWriter out = response.getWriter();
			out.write(controller.execute(request));
			out.close();
		}
	}

	@Override
	public void init() throws ServletException {
		Logger log = LoggerFactory.getLogger(ControllerServlet.class);
		log.info("Initializing servlet...");
		
		// Load model
        model = Model.getInstance();
        
        // Load config
        String configPath = System.getProperty("user.home") + "/.jotp/config.properties";
        log.info("Attempting to load configuration from file: " + configPath);
        try {
			config = PropertiesConfiguration.loadPropertiesConfiguration(configPath);
		} catch (ConfigurationException e) {
			log.error("An error occurred while loading configuration: " + e.getMessage());
			throw new ServletException("An error occurred during configuration.  See the jOTP log for details.");
		}
        log.info("Configuration loaded successfully.");
        
        // Map controllers to URLs
        MonitorController mc = new MonitorController();
        getControllers = new HashMap<String, Controller>();
        getControllers.put("/monitor", mc);
        
        postControllers = new HashMap<String, Controller>();
        postControllers.put("/monitor", mc);
        postControllers.put("/text", new TextOtpController(config, model));
        postControllers.put("/email", new EmailOtpController(config, model));
        postControllers.put("/validate", new OtpValidationController(model));
        
        // Start reaper thread
        reaper = new Reaper(model);
        reaper.start();
	}
}
