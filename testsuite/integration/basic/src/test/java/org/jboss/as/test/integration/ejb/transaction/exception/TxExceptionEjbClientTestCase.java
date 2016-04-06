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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ejb.remote.common.EJBManagementUtil;
import org.jboss.as.test.integration.ejb.transaction.exception.TestXAResource.CommitOperation;
import org.jboss.ejb.client.EJBClient;
import org.jboss.ejb.client.EJBClientTransactionContext;
import org.jboss.ejb.client.StatelessEJBLocator;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that remote EJB client gets appropriate exception when TM throws
 * exception.
 */
@RunAsClient
@RunWith(Arquillian.class)
public class TxExceptionEjbClientTestCase extends TxExceptionBaseTestCase {

    private static Logger LOG = Logger.getLogger(TxExceptionEjbClientTestCase.class);

    private static String nodeName;

    @BeforeClass
    public static void beforeClass() throws Exception {
        nodeName = EJBManagementUtil.getNodeName();
    }

    /**
     * Create and setup the EJB client context backed by the remoting receiver
     */
    @Before
    public void beforeTest() throws Exception {
        final EJBClientTransactionContext localUserTxContext = EJBClientTransactionContext.createLocal();
        EJBClientTransactionContext.setGlobalContext(localUserTxContext);
    }

    @Override
    protected void checkReceivedException(Exception e, ThrownExceptionType thrownExceptionType) {
        switch (thrownExceptionType) {
        case FROM_BEAN_METHOD_WHICH_RUNS_IN_TRANSACTION_STARTED_BY_CALLER:
            assertThat("TransactionRolledbackException should be thrown.", e.getClass(), equalTo(javax.transaction.TransactionRolledbackException.class));
            break;
        case FROM_BEAN_METHOD_WHICH_STARTED_CONTAINER_MANAGED_TRANSACTION:
            // FIXME
            assertThat("TransactionRolledbackException should be thrown.", e.getClass(), equalTo(javax.transaction.TransactionRolledbackException.class));
            break;
        case FROM_BEAN_METHOD_WHICH_STARTED_BEAN_MANAGED_TRANSACTION:
            assertThat("RemoteException should be thrown.", e.getClass(), equalTo(java.rmi.RemoteException.class));
            break;
        default:
            Assert.fail();
        }
    }

    @Test
    public void testBMTxHeuristicExceptionIsPropagatedToClient() throws Exception {
        StatelessBeanRemote bean = getCMTBean();
        Assert.assertNotNull(bean);
        try {
            final UserTransaction userTransaction = getUserTransaction();
            userTransaction.begin();
            bean.testTwoResourceTransactionMandatory(CommitOperation.THROW_KNOWN_XA_EXCEPTION);
            userTransaction.commit();
            // TODO check with specification
            Assert.fail("It was expected a SystemException being thrown.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            assertThat("SystemException should be thrown.", e.getClass(), equalTo(javax.transaction.SystemException.class));
        }
    }

    @Ignore("JBEAP-165")
    @Test
    public void testDriverSpecificExceptionIsNotPropagatedToClient() throws Exception {
//        super.testDriverSpecificExceptionIsNotPropagatedToClient();
    }

    @Override
    protected UserTransaction getUserTransaction() {
        return EJBClient.getUserTransaction(nodeName);
    }

    @Override
    protected StatelessBeanRemote getCMTBean() {
        final StatelessEJBLocator<StatelessBeanRemote> remoteBeanLocator = new StatelessEJBLocator<StatelessBeanRemote>(StatelessBeanRemote.class,
                APP_NAME, MODULE_NAME, StatelessCMTBean.class.getSimpleName(), "");
        return EJBClient.createProxy(remoteBeanLocator);
    }

    @Override
    protected IStatelessBean getBMTBean() {
        final StatelessEJBLocator<StatelessBeanRemote> remoteBeanLocator = new StatelessEJBLocator<StatelessBeanRemote>(StatelessBeanRemote.class,
                APP_NAME, MODULE_NAME, StatelessBMTBean.class.getSimpleName(), "");
        return EJBClient.createProxy(remoteBeanLocator);
    }
}
