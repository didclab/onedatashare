package org.onedatashare.server.service;


import junit.framework.TestCase;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;
import org.onedatashare.server.module.BoxResource;
import reactor.core.publisher.Mono;
import org.junit.Assert;

public class BoxResourceTest extends TestCase {

    BoxResource testObj;
    String accountId;

    public OAuthEndpointCredential createEndpointCredential(){
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential("testObj");
        oAuthEndpointCredential.setToken("8TVTg5AqVQTF76Jv0oMDP3A99DTJHDxm");
        oAuthEndpointCredential.setAccountId("jacobTestAccountId");
        return  oAuthEndpointCredential;
    }

    public void testListRoot(){
        testObj = new BoxResource(createEndpointCredential());
        ListOperation listOperation = new ListOperation(accountId, "","");// zero is the root dir in Box
        Stat parent = testObj.list(listOperation);
        Assert.assertTrue(parent.getId() != null && !parent.getId().isEmpty());
        Assert.assertTrue(parent.isDir() || parent.isFile());
        for(Stat stat : parent.getFilesList()){
            System.out.println(stat.toString());
            Assert.assertTrue(stat.getId() != null && !stat.getId().isEmpty());
            Assert.assertTrue(stat.isDir() || stat.isFile());
        }
    }

    public void testListApacheMaven(){
        testObj = new BoxResource(createEndpointCredential());
        ListOperation listOperation = new ListOperation(accountId, "","138759045640");// zero is the root dir in Box
        Stat parent = testObj.list(listOperation);
        Assert.assertTrue(parent.getId() != null && !parent.getId().isEmpty());
        Assert.assertTrue(parent.isDir() || parent.isFile());
        for(Stat stat : parent.getFilesList()){
            System.out.println(stat.toString());
            Assert.assertTrue(stat.getId() != null && !stat.getId().isEmpty());
            Assert.assertTrue(stat.isDir() || stat.isFile());
        }
    }

    public void testMkdirInRoot(){
        String folderName = "doesthisworkomg";
        testObj = new BoxResource(createEndpointCredential());
        MkdirOperation mkdirOperation = new MkdirOperation();
        mkdirOperation.setPath("");
        mkdirOperation.setCredId("fakekeyName");
        mkdirOperation.setId("0"); //you can find the folder id in the url of your box account when navigating the site
        mkdirOperation.setFolderToCreate(folderName);
        testObj.mkdir(mkdirOperation);
        ListOperation listOperation = new ListOperation(accountId, "", mkdirOperation.getId());// zero is the root dir in Box
        Stat parent = testObj.list(listOperation);

    }

}
