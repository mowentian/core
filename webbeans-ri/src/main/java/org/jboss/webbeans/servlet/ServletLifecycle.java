/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.webbeans.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

/**
 * Reacts to phases of the servlet life cycles
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class ServletLifecycle
{
   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);
   // The servlet context
   private static ServletContext servletContext;
   
   /**
    * Starts the application
    * 
    * Runs the bootstrapper for bean discover and initialization
    * 
    * @param context The servlet context
    */
   public static void beginApplication(ServletContext context)
   {
      servletContext = context;
      ApplicationContext.INSTANCE.setBeanMap(new ApplicationBeanMap(servletContext));
      WebBeansBootstrap webBeansBootstrap = new WebBeansBootstrap();
      CurrentManager.rootManager().addContext(RequestContext.INSTANCE);
      CurrentManager.rootManager().addContext(SessionContext.INSTANCE);
      CurrentManager.rootManager().addContext(ApplicationContext.INSTANCE);
      webBeansBootstrap.boot(getWebBeanDiscovery());
   }
   
   /**
    * Ends the application
    */
   public static void endApplication() 
   {
      ApplicationContext.INSTANCE.destroy();
      ApplicationContext.INSTANCE.setBeanMap(null);
      servletContext = null;
   }
   
   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public static void beginSession(HttpSession session)
   {
   }
   
   /**
    * Ends a session
    * 
    * @param session The HTTP session
    */
   public static void endSession(HttpSession session) 
   {
      SessionContext.INSTANCE.setBeanMap(new SessionBeanMap(session));
      SessionContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
   }   
   
   /**
    * Begins a HTTP request 
    * 
    * Sets the session into the session context
    * 
    * @param request The request
    */
   public static void beginRequest(HttpServletRequest request) 
   {
      SessionContext.INSTANCE.setBeanMap(new SessionBeanMap(request.getSession()));
      DependentContext.INSTANCE.setActive(true);
   }
   
   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public static void endRequest(HttpServletRequest request) 
   {
      DependentContext.INSTANCE.setActive(false);
      RequestContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
   }
   
   /**
    * Gets the servlet context
    * 
    * @return The servlet context
    */
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
   /**
    * Gets the Web Beans discovery class
    * 
    * @return The discoverer
    */
   private static WebBeanDiscovery getWebBeanDiscovery()
   {
      WebBeanDiscovery webBeanDiscovery = null;

      for (Class<? extends WebBeanDiscovery> clazz : WebBeansBootstrap.getWebBeanDiscoveryClasses())
      {
         Constructor<? extends WebBeanDiscovery> constructor = Reflections.getConstructor(clazz, ServletContext.class);
         if (constructor != null)
         {
            try
            {
               webBeanDiscovery = constructor.newInstance(servletContext);
               break;
            }
            catch (InstantiationException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (IllegalAccessException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (IllegalArgumentException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (InvocationTargetException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
         }
      }
      return webBeanDiscovery;
   }
   
}
