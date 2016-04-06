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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ejb.transaction.exception.TestXAResource.CommitOperation;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that container behaves according to the specification when TM throws
 * exception.
 */
@RunWith(Arquillian.class)
public class TxExceptionTestCase extends TxExceptionBaseTestCase {

    private static Logger LOG = Logger.getLogger(TxExceptionTestCase.class);

    @Override
    protected void checkReceivedException(Exception e, ThrownExceptionType thrownExceptionType) {
        switch (thrownExceptionType) {
        case FROM_BEAN_METHOD_WHICH_RUNS_IN_TRANSACTION_STARTED_BY_CALLER:
            assertThat("EJBTransactionRolledbackException should be thrown.", e.getClass(), equalTo(javax.ejb.EJBTransactionRolledbackException.class));
            break;
        case FROM_BEAN_METHOD_WHICH_STARTED_CONTAINER_MANAGED_TRANSACTION:
        case FROM_BEAN_METHOD_WHICH_STARTED_BEAN_MANAGED_TRANSACTION:
            assertThat("EJBException should be thrown.", e.getClass(), equalTo(javax.ejb.EJBException.class));
            break;
        default:
            Assert.fail();
        }
    }

    @Test
    public void testCMTxHeuristicExceptionIsPropagatedToClient() throws Exception {
        IStatelessBean bean = getCMTBean();
        try {
            bean.testTwoResourceTransaction(CommitOperation.THROW_KNOWN_XA_EXCEPTION);
            Assert.fail("It was expected a EJBException being thrown.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            assertThat("EJBException should be thrown.", e.getClass(), equalTo(javax.ejb.EJBException.class));
            assertThat("HeuristicMixedException should be propagated.", e.getCause().getClass(), equalTo(javax.transaction.HeuristicMixedException.class));
        }
    }

    @Test
    public void testBMTxHeuristicExceptionIsPropagatedToClient() throws Exception {
        IStatelessBean bean = getCMTBean();
        Assert.assertNotNull(bean);
        try {
            final UserTransaction userTransaction = getUserTransaction();
            userTransaction.begin();
            bean.testTwoResourceTransactionMandatory(CommitOperation.THROW_KNOWN_XA_EXCEPTION);
            userTransaction.commit();
            Assert.fail("It was expected a HeuristicMixedException being thrown.");
        } catch (Exception e) {
            LOG.infof(e, "Client got exception: %s", e.getMessage());
            assertThat("HeuristicMixedException should be thrown.", e.getClass(), equalTo(javax.transaction.HeuristicMixedException.class));
        }
    }

    @Test
    public void testDriverSpecificExceptionIsNotPropagatedToClient() throws Exception {
        IStatelessBean bean = getCMTBean();
        try {
            bean.testTwoResourceTransaction(CommitOperation.THROW_UNKNOWN_XA_EXCEPTION);
            Assert.fail("It was expected a HeuristicMixedException being thrown.");
        } catch (Exception e) {
            LOG.infof("Client got exception: %s", e.toString());
            assertThat(e.toString(), not(containsString("java.lang.ClassNotFoundException: org.postgresql.xa.PGXAException")));
            assertThat(e.toString(), containsString("HeuristicMixedException"));
        }
    }

    @Override
    protected StatelessBeanLocal getCMTBean() {
        try {
            return StatelessBeanLocal.class.cast(
                    iniCtx.lookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + StatelessCMTBean.class.getSimpleName() + "!" + StatelessBeanLocal.class.getName()));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected IStatelessBean getBMTBean() {
        try {
            return StatelessBeanLocal.class.cast(
                    iniCtx.lookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + StatelessBMTBean.class.getSimpleName() + "!" + StatelessBeanLocal.class.getName()));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
