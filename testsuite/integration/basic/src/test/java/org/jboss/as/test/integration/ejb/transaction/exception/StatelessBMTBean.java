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

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.as.test.integration.ejb.transaction.exception.TestXAResource.CommitOperation;

@Stateless
@Remote(StatelessBeanRemote.class)
@RemoteHome(StatelessBeanHome.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class StatelessBMTBean implements StatelessBeanLocal {

    @Resource  
    private UserTransaction utx;  
    
    @Override
    public void throwRuntimeException() {
        try {
            utx.begin();
        } catch (NotSupportedException | SystemException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
    
    @Override
    public void testTwoResourceTransaction(CommitOperation secondResourceCommitOp) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testTwoResourceTransactionMandatory(CommitOperation secondResourceCommitOp) throws Exception {
        throw new UnsupportedOperationException();
    }

}
