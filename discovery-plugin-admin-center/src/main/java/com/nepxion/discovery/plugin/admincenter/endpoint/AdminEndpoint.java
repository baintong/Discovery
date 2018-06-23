package com.nepxion.discovery.plugin.admincenter.endpoint;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractMvcEndpoint;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nepxion.discovery.plugin.framework.cache.PluginCache;
import com.nepxion.discovery.plugin.framework.constant.PluginConstant;
import com.nepxion.discovery.plugin.framework.exception.PluginException;

@ManagedResource(description = "Admin Endpoint")
@SuppressWarnings("unchecked")
public class AdminEndpoint extends AbstractMvcEndpoint implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(AdminEndpoint.class);

    private ConfigurableApplicationContext applicationContext;
    @SuppressWarnings("rawtypes")
    private ServiceRegistry serviceRegistry;
    private Registration registration;

    @Autowired
    private PluginCache pluginCache;

    @SuppressWarnings("rawtypes")
    public AdminEndpoint(ServiceRegistry serviceRegistry) {
        super("/admin", true, true);

        this.serviceRegistry = serviceRegistry;
    }

    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    @RequestMapping(path = "blacklist", method = RequestMethod.GET)
    @ResponseBody
    @ManagedOperation
    public Object blacklist(@RequestParam("serviceId") String serviceId, @RequestParam("ip") String ip) {
        Boolean discoveryControlEnabled = getEnvironment().getProperty(PluginConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED, Boolean.class, Boolean.TRUE);
        if (!discoveryControlEnabled) {
            return new ResponseEntity<>(Collections.singletonMap("Message", "Discovery control is disabled"), HttpStatus.NOT_FOUND);
        }

        if (registration == null) {
            throw new PluginException("No registration found");
        }

        pluginCache.put(serviceId, ip);

        LOG.info("Add blacklist for serviceId={}, ip={} successfully", serviceId, ip);

        return "success";
    }

    @RequestMapping(path = "clear", method = RequestMethod.GET)
    @ResponseBody
    @ManagedOperation
    public Object clear(@RequestParam("serviceId") String serviceId) {
        Boolean discoveryControlEnabled = getEnvironment().getProperty(PluginConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED, Boolean.class, Boolean.TRUE);
        if (!discoveryControlEnabled) {
            return new ResponseEntity<>(Collections.singletonMap("Message", "Discovery control is disabled"), HttpStatus.NOT_FOUND);
        }

        if (registration == null) {
            throw new PluginException("No registration found");
        }

        pluginCache.clear(serviceId);

        LOG.info("Clear blacklist for serviceId={} successfully", serviceId);

        return "success";
    }

    @RequestMapping(path = "status", method = RequestMethod.POST)
    @ResponseBody
    @ManagedOperation
    public Object status(@RequestBody String status) {
        Boolean discoveryControlEnabled = getEnvironment().getProperty(PluginConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED, Boolean.class, Boolean.TRUE);
        if (!discoveryControlEnabled) {
            return new ResponseEntity<>(Collections.singletonMap("Message", "Discovery control is disabled"), HttpStatus.NOT_FOUND);
        }

        if (registration == null) {
            throw new PluginException("No registration found");
        }

        serviceRegistry.setStatus(registration, status);

        LOG.info("Set status for serviceId={} status={} successfully", registration.getServiceId(), status);

        return "success";
    }

    @RequestMapping(path = "deregister", method = RequestMethod.POST)
    @ResponseBody
    @ManagedOperation
    public Object deregister() {
        Boolean discoveryControlEnabled = getEnvironment().getProperty(PluginConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED, Boolean.class, Boolean.TRUE);
        if (!discoveryControlEnabled) {
            return new ResponseEntity<>(Collections.singletonMap("Message", "Discovery control is disabled"), HttpStatus.NOT_FOUND);
        }

        if (registration == null) {
            throw new PluginException("No registration found");
        }

        serviceRegistry.deregister(registration);

        LOG.info("Deregister for serviceId={} successfully", registration.getServiceId());

        return "success";
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        }
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}