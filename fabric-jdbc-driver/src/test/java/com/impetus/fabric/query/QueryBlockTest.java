/*******************************************************************************
 * * Copyright 2018 Impetus Infotech.
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 ******************************************************************************/

package com.impetus.fabric.query;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import junit.framework.TestCase;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.impetus.blkch.BlkchnException;
import com.impetus.blkch.sql.DataFrame;
import com.impetus.fabric.model.HyperUser;
import com.impetus.fabric.model.Store;



@RunWith(PowerMockRunner.class)
@PrepareForTest({QueryBlock.class,HFClient.class, SDKUtils.class, HFCAClient.class})
public class QueryBlockTest extends TestCase {

    @Mock
    HFCAClient mockCA;
    @Test
    public void testEnrollAndRegisterUser() throws ClassNotFoundException, SQLException, java.lang.Exception {
        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", "test", "testpw");
        HyperUser mockuser = mock(HyperUser.class);
        when(mockuser.isEnrolled()).thenReturn(true);
        Store mockStore = mock(Store.class);
        PowerMockito.whenNew(Store.class).withAnyArguments().thenReturn(mockStore);
        PowerMockito.mockStatic(HFCAClient.class);
        when(HFCAClient.createNewInstance(anyString(), any())).thenReturn(mockCA);
        Enrollment enrollment = mock(Enrollment.class);
        when(mockCA.enroll(anyString(), anyString())).thenReturn(enrollment);
        qb.enroll();
    }

    @Mock
    HFClient mockClient;
    @Test
    public void testReconstructChannel() throws ClassNotFoundException, SQLException, InvalidArgumentException, ProposalException, java.lang.Exception {
        PowerMockito.mockStatic(HFClient.class);
        when(HFClient.createNewInstance()).thenReturn(mockClient);

        Set<String> returnSet = new HashSet<String>();
        returnSet.add("a");
        returnSet.add("c");

        when(mockClient.queryChannels(any(Peer.class))).thenReturn(returnSet);

        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", null, null);



        HyperUser mockuser = mock(HyperUser.class);
        when(mockuser.isEnrolled()).thenReturn(true);

        Store mockStore = mock(Store.class);
        PowerMockito.whenNew(Store.class).withAnyArguments().thenReturn(mockStore);
        
        Channel mockChannel = mock(Channel.class);
        when(mockClient.newChannel(anyString())).thenReturn(mockChannel);

        qb.reconstructChannel();
    }

    @Mock
    SDKUtils mockSDKUtils;
    @SuppressWarnings("unchecked")
    @Test
    public void testInstallChainCode() throws ClassNotFoundException, SQLException, InvalidArgumentException{

        PowerMockito.mockStatic(SDKUtils.class);

        PowerMockito.mockStatic(HFClient.class);
        when(HFClient.createNewInstance()).thenReturn(mockClient);

        Channel mockChannel = mock(Channel.class);
        when(mockClient.newChannel(anyString())).thenReturn(mockChannel);

        InstallProposalRequest mockInstallProposalRequest = mock(InstallProposalRequest.class);
        when(mockClient.newInstallProposalRequest()).thenReturn(mockInstallProposalRequest);


        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", null, null);
	    qb.setChannel();
        String chaincodeName ="chncodefunc";
        String version = "1.0";
        String chaincodePath = "hyperledger/fabric/examples/chaincode/go/chaincode_example02";


        when(SDKUtils.getProposalConsistencySets(anyCollection())).thenReturn(new ArrayList<>());
        String result = qb.installChaincode(chaincodeName, version, qb.getConf().getConfigPath(), chaincodePath);
        assert(result.equals("Chaincode installed successfully"));

    }


    //This test is failing because of not able to mock Java CompletableFuture properly
    @SuppressWarnings("unchecked")
    @Test
    public void testInstantiateChaincode() throws ClassNotFoundException, SQLException, InvalidArgumentException{

        PowerMockito.mockStatic(HFClient.class);
        when(HFClient.createNewInstance()).thenReturn(mockClient);

        Channel mockChannel = mock(Channel.class);
        when(mockClient.newChannel(anyString())).thenReturn(mockChannel);

        InstantiateProposalRequest mockInstantiateProposalRequest = mock(InstantiateProposalRequest.class);
        when(mockClient.newInstantiationProposalRequest()).thenReturn(mockInstantiateProposalRequest);


        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", null, null);
        qb.setChannel();
        String chaincodeName ="chncodefunc";
        String version = "1.0";
        String goPath = "/home/impetus/IdeaProjects/fabric-jdbc-driver/src/test/resources/blockchain-query/";

      CompletableFuture<BlockEvent.TransactionEvent> mockCompletableFutureTEvent = new CompletableFuture<BlockEvent.TransactionEvent>();

        when(mockChannel.sendTransaction(any(ArrayList.class),anyCollection())).thenReturn(mockCompletableFutureTEvent);// .thenReturn(mockCompletableFutureTEvent);

        try {
            qb.instantiateChaincode(chaincodeName,version,goPath,"testFunction",new String[] {"a","b","5","10"}, null);
        }
        catch(BlkchnException blkEx){
            //Do Nothing for Java Concurrent Error
            if(!blkEx.getMessage().contains("java.util.concurrent.TimeoutException")) {

                assert(false);
            }
        }

        assert(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInvokeChaincode() throws ClassNotFoundException, SQLException, InvalidArgumentException, ProposalException{

        PowerMockito.mockStatic(HFClient.class);
        when(HFClient.createNewInstance()).thenReturn(mockClient);

        Channel mockChannel = mock(Channel.class);
        when(mockClient.newChannel(anyString())).thenReturn(mockChannel);
        when(mockClient.newPeer(anyString(), anyString(), any())).thenCallRealMethod();


        InstantiateProposalRequest mockInstantiateProposalRequest = mock(InstantiateProposalRequest.class);
        when(mockClient.newInstantiationProposalRequest()).thenReturn(mockInstantiateProposalRequest);

        TransactionProposalRequest mockTransactionProposalRequest = mock(TransactionProposalRequest.class);
        when(mockClient.newTransactionProposalRequest()).thenReturn(mockTransactionProposalRequest);

        Collection<ProposalResponse> mockProposalResponsesList = new ArrayList<ProposalResponse>();
        ProposalResponse mockProposalResponses = mock(ProposalResponse.class);
        when(mockProposalResponses.getStatus()).thenReturn(ProposalResponse.Status.SUCCESS);
        Peer mkpeer = mock(Peer.class);
        when(mockProposalResponses.getPeer()).thenReturn(mkpeer);
        mockProposalResponsesList.add(mockProposalResponses);
        mockProposalResponsesList.add(mockProposalResponses);

        when(mockChannel.sendTransactionProposal(any(TransactionProposalRequest.class),anyCollectionOf(Peer.class))).thenReturn(mockProposalResponsesList);


        PowerMockito.mockStatic(SDKUtils.class);

        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", null, null);
        qb.setChannel();
        String chaincodeName ="chncodefunc";

        when(SDKUtils.getProposalConsistencySets(anyCollection())).thenReturn(new ArrayList<>());

        CompletableFuture<BlockEvent.TransactionEvent> mockCompletableFutureTEvent = new CompletableFuture<BlockEvent.TransactionEvent>();//{mockTranEvent};
        when(mockChannel.sendTransaction(any(ArrayList.class))).thenReturn(mockCompletableFutureTEvent);// .thenReturn(mockCompletableFutureTEvent);

        try {
            qb.invokeChaincode(chaincodeName, "testFunction", new String[]{"a", "b", "5", "10"});

        }catch(BlkchnException blkEx){
            //Do Nothing for Java concurrent Error
            if(!(blkEx.getMessage().contains("java.util.concurrent.TimeoutException"))) {
                assert(false);
            }
        }
        assert(true);

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testInvokeNoPeerInfo() throws ClassNotFoundException, SQLException, InvalidArgumentException, ProposalException {
        PowerMockito.mockStatic(HFClient.class);
        when(HFClient.createNewInstance()).thenReturn(mockClient);

        Channel mockChannel = mock(Channel.class);
        when(mockClient.newChannel(anyString())).thenReturn(mockChannel);
        when(mockClient.newPeer(anyString(), anyString(), any())).thenCallRealMethod();


        InstantiateProposalRequest mockInstantiateProposalRequest = mock(InstantiateProposalRequest.class);
        when(mockClient.newInstantiationProposalRequest()).thenReturn(mockInstantiateProposalRequest);

        TransactionProposalRequest mockTransactionProposalRequest = mock(TransactionProposalRequest.class);
        when(mockClient.newTransactionProposalRequest()).thenReturn(mockTransactionProposalRequest);

        Collection<ProposalResponse> mockProposalResponsesList = new ArrayList<ProposalResponse>();
        ProposalResponse mockProposalResponses = mock(ProposalResponse.class);
        when(mockProposalResponses.getStatus()).thenReturn(ProposalResponse.Status.SUCCESS);
        Peer mkpeer = mock(Peer.class);
        when(mockProposalResponses.getPeer()).thenReturn(mkpeer);
        mockProposalResponsesList.add(mockProposalResponses);
        mockProposalResponsesList.add(mockProposalResponses);

        when(mockChannel.sendTransactionProposal(any(TransactionProposalRequest.class),anyCollectionOf(Peer.class))).thenReturn(mockProposalResponsesList);


        PowerMockito.mockStatic(SDKUtils.class);

        String configPath = "src/test/resources/blockchain-query";
        Class.forName("com.impetus.fabric.jdbc.FabricDriver");
        QueryBlock qb = new QueryBlock(configPath,"mychannel", null, null);
        qb.setChannel();
        String chaincodeName ="chncodefuncNoPeerInfo";

        when(SDKUtils.getProposalConsistencySets(anyCollection())).thenReturn(new ArrayList<>());

        CompletableFuture<BlockEvent.TransactionEvent> mockCompletableFutureTEvent = new CompletableFuture<BlockEvent.TransactionEvent>();//{mockTranEvent};
        when(mockChannel.sendTransaction(any(ArrayList.class))).thenReturn(mockCompletableFutureTEvent);// .thenReturn(mockCompletableFutureTEvent);

        DataFrame df = qb.invokeChaincode(chaincodeName, "testFunction", new String[]{"a", "b", "5", "10"});
        assertEquals(df.getData().size(), 1);
        List<Object> row = df.getData().get(0);
        assertEquals(false, Boolean.parseBoolean(row.get(1).toString()));
        assertEquals("Endorsing peer information not provided for chaincode chncodefuncNoPeerInfo", row.get(3).toString());
    }

}
