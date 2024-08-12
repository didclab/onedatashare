package org.onedatashare.server.module;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.AccountEndpointCredential;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import reactor.core.publisher.Mono;

public class HttpResourceTest {

    HttpResource testObj;

    public AccountEndpointCredential testAccountCredential(){
        AccountEndpointCredential accountEndpointCredential = new AccountEndpointCredential();
        accountEndpointCredential.setAccountId("test_account_id");
        accountEndpointCredential.setUri("http://localhost:80/");
        accountEndpointCredential.setUsername("test");
        accountEndpointCredential.setSecret("test");
        return accountEndpointCredential;
    }

    @Test
    public void testListingRootOfNginx() {
        //String credId, String path, String id
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", "", "");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFilesList());
        Assert.assertNotNull(result.getName());
        Assert.assertNotNull(result.getId());
        Assert.assertNotNull(result.getSize());
    }

    @Test
    public void testListingRootThreeFilesTwoFolders(){
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", "", "");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFiles());
        for(Stat child : result.getFiles()){
            System.out.println("Name: " + child.getName() + " Size: " + child.getSize());
        }
        Assert.assertTrue(result.getFiles().length == 5);
    }

    @Test
    public void testListConcurrencyDataSet(){
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", "concurrency/", "");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFiles());
        for(Stat child : result.getFiles()){
            System.out.println("Name: " + child.getName() + " Size: " + child.getSize());
        }
        Assert.assertTrue(result.getFiles().length == 70);
    }

    @Test
    public void testListParallelDataSet(){
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", "parallel/", "");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFiles());
        for(Stat child : result.getFiles()){
            System.out.println("Name: " + child.getName() + " Size: " + child.getSize());
        }
        Assert.assertTrue(result.getFiles().length == 7);
    }

    @Test
    public void testListParallelDataSetUsingId(){
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", null, "parallel/");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFiles());
        for(Stat child : result.getFiles()){
            System.out.println("Name: " + child.getName() + " Size: " + child.getSize());
        }
        Assert.assertTrue(result.getFiles().length == 7);
    }

    @Test
    public void testListConcurrencyDataSetUsingId(){
        this.testObj = new HttpResource(this.testAccountCredential());
        ListOperation listOperation = new ListOperation("", null, "parallel/");
        Stat result = this.testObj.list(listOperation);
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getFiles());
        for(Stat child : result.getFiles()){
            System.out.println("Name: " + child.getName() + " Size: " + child.getSize());
        }
        Assert.assertTrue(result.getFiles().length == 7);
    }

}
