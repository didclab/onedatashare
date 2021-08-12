package org.onedatashare.server.module;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.OAuthEndpointCredential;
import org.onedatashare.server.model.filesystem.operations.DeleteOperation;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.filesystem.operations.MkdirOperation;

import static org.junit.jupiter.api.Assertions.*;

class DropboxResourceTest {

    DropboxResource testObj;
    String clientIdentifier = "OneDataShare-DIDCLab";

    public OAuthEndpointCredential oAuthEndpointCredentialWithDevToken() {
        OAuthEndpointCredential oAuthEndpointCredential = new OAuthEndpointCredential("testid");
        return oAuthEndpointCredential;
    }

    @Test
    void deleteRecurssiveFolder() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        DeleteOperation deleteOperation = new DeleteOperation("","","");
        deleteOperation.setToDelete("143081650992");
        testObj.delete(deleteOperation).block();
        Assert.assertTrue("",true);
    }

    @Test
    void deleteFolder(){
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        DeleteOperation deleteOperation = new DeleteOperation("","","");
        deleteOperation.setToDelete("/nested/helloBQZ/ha");
        testObj.delete(deleteOperation).block();
        Assert.assertTrue("",true);
    }

    @Test
    void deleteFile(){
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        DeleteOperation deleteOperation = new DeleteOperation("","","");
        deleteOperation.setToDelete("Test.txt");
        testObj.delete(deleteOperation).block();
        Assert.assertTrue("",true);
    }


    @Test
    void listRootUsinglLash() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        ListOperation listOperation = new ListOperation("", "", "/");
        Stat stat = testObj.list(listOperation).block();
        Assert.assertTrue(stat != null);
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            Assert.assertTrue(childs.getId() != null && !childs.getId().isEmpty());
            Assert.assertTrue(childs.getName() != null && !childs.getName().isEmpty());

        }
    }

    @Test
    void listRootUsingNothing() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        ListOperation listOperation = new ListOperation("", "", "");
        Stat stat = testObj.list(listOperation).block();
        Assert.assertTrue(stat != null);
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            Assert.assertTrue(childs.getId() != null && !childs.getId().isEmpty());
            Assert.assertTrue(childs.getName() != null && !childs.getName().isEmpty());

        }
    }

    @Test
    void list142929312630UsingZero() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        ListOperation listOperation = new ListOperation("", "", "142929312630");
        Stat stat = testObj.list(listOperation).block();
        Assert.assertTrue(stat != null);
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            Assert.assertTrue(childs.getId() != null && !childs.getId().isEmpty());
            Assert.assertTrue(childs.getName() != null && !childs.getName().isEmpty());
        }
    }

    @Test
    void listNested() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        ListOperation listOperation = new ListOperation("", "", "id:SQhR5zO0sjAAAAAAAAAADQ");
        Stat stat = testObj.list(listOperation).block();
        Assert.assertTrue(stat != null);
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            System.out.println(childs.getSize());
            Assert.assertTrue(childs.getSize() > 0);
            Assert.assertTrue(childs.getId() != null && !childs.getId().isEmpty());
            Assert.assertTrue(childs.getName() != null && !childs.getName().isEmpty());
        }
    }

    @Test
    void mkdirInRoot() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        String name = "helloworldd";
        boolean found = false;
        MkdirOperation mkdirOperation = new MkdirOperation();
        mkdirOperation.setPath("");
        mkdirOperation.setFolderToCreate(name);
        mkdirOperation.setCredId("");
        mkdirOperation.setId("/helloworld/hello");
        testObj.mkdir(mkdirOperation).block();
        Stat stat = testObj.list(new ListOperation("", "", "")).block();
        Assert.assertTrue(stat != null);
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            if (childs.getName().equals(name)) {
                Assert.assertTrue(true);
                found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @Test
    void mkdirInNested() {
        testObj = new DropboxResource(oAuthEndpointCredentialWithDevToken(), clientIdentifier);
        String parentId = "/nested/helloBQZ/";
        String nameToCreate = "ha";
        boolean found = false;
        MkdirOperation mkdirOperation = new MkdirOperation();
        mkdirOperation.setPath("");
        mkdirOperation.setFolderToCreate(nameToCreate);
        mkdirOperation.setCredId("");
        mkdirOperation.setId(parentId);
        testObj.mkdir(mkdirOperation).block();
        Stat stat = testObj.list(new ListOperation("", "", "/nested/helloBQZ/")).block();
        for (Stat childs : stat.getFilesList()) {
            System.out.println(childs.toString());
            System.out.println(childs.getId());
            if (childs.getName().equals(nameToCreate)) {
                Assert.assertTrue(true);
                found = true;
            }
        }
        Assert.assertTrue(found);

    }
}