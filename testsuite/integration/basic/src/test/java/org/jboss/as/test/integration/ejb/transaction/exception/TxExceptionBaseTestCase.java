/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.ejb.transaction.exception;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that container behaves according to the specification when exception is thrown.
 */
public abstract class TxExceptionBaseTestCase {

    protected static final String APP_NAME = "tx-exception-test";
    protected static final String MODULE_NAME = "ejb";

    private static Logger LOG = Logger.getLogger(TxExceptionBaseTestCase.class);

    public static enum ThrownExceptionType {
        FROM_BEAN_METHOD_WHICH_RUNS_IN_TRANSACTION_STARTED_BY_CALLER,
        FROM_BEAN_METHOD_WHICH_STARTED_CONTAINER_MANAGED_TRANSACTION,
        FROM_BEAN_METHOD_WHICH_STARTED_BEAN_MANAGED_TRANSACTION
    }

    @ArquillianResource
    protected InitialContext iniCtx;
    @Inject
    private UserTransaction userTransaction;

    @Deployment
    public static Archive<?> createDeployment() {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear");
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, MODULE_NAME + ".jar");
        jar.addPackage(TxExceptionBaseTestCase.class.getPackage());
        jar.addPackages(true, "javassist");
        ear.addAsModule(jar);
        return ear;
    }

    /**
     * Exception thrown from bean method which runs in context of caller's transaction 
     */
    @Test
    public void exceptionThrownFromBeanMethodWhichRunsInTransactionStartedByCaller(){
        try {
            final UserTransaction userTransaction = getUserTransaction();
            userTransaction.begin();
            getCMTBean().throwRuntimeException();
            Assert.fail("An exception was expected.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            checkReceivedException(e, ThrownExceptionType.FROM_BEAN_METHOD_WHICH_RUNS_IN_TRANSACTION_STARTED_BY_CALLER);
        }
    }

    /**
     * Exception thrown from bean method which started container managed transaction 
     */
    @Test
    public void exceptionThrownFromBeanMethodWhichStartedContainerManagedTransaction(){
        try {
            getCMTBean().throwRuntimeException();
            Assert.fail("An exception was expected.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            checkReceivedException(e, ThrownExceptionType.FROM_BEAN_METHOD_WHICH_STARTED_CONTAINER_MANAGED_TRANSACTION);
        }
    }

    /**
     * Exception thrown from bean method which started bean managed transaction 
     */
    @Test
    public void exceptionThrownFromBeanMethodWhichStartedBeanManagedTransaction(){
        try {
            getBMTBean().throwRuntimeException();
            Assert.fail("An exception was expected.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            checkReceivedException(e, ThrownExceptionType.FROM_BEAN_METHOD_WHICH_STARTED_BEAN_MANAGED_TRANSACTION);
        }
    }

    protected abstract void checkReceivedException(Exception e, ThrownExceptionType fromBeanMethodWhichRunsInTransactionStartedByCaller);

    protected abstract IStatelessBean getCMTBean();

    protected abstract IStatelessBean getBMTBean();

    protected UserTransaction getUserTransaction() {
        return userTransaction;
    }

}
