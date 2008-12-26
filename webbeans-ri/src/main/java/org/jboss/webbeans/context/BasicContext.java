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

package org.jboss.webbeans.context;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.context.beanmap.BeanMap;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;

/**
 * The abstraction of a private context, on that operates on a ThreadLocal
 * BeanMap and ThreadLocal active state
 * 
 * A private context doesn't rely on some external context to hold it's state
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.context.DependentContext
 * @see org.jboss.webbeans.context.RequestContext
 * @see org.jboss.webbeans.context.ConversationContext
 * @see org.jboss.webbeans.context.SessionContext
 */
public abstract class BasicContext extends AbstractBeanMapContext
{
   // The beans
   protected ThreadLocal<BeanMap> beans;

   /**
    * Constructor 
    * 
    * @param scopeType The scope types
    */
   public BasicContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new ThreadLocal<BeanMap>()
      {
         
         @Override
         protected BeanMap initialValue()
         {
            return new SimpleBeanMap();
         }
         
      };
   }

   /**
    * Delegates to a ThreadLocal instance
    */
   @Override
   protected BeanMap getBeanMap()
   {
      return beans.get();
   }

}
