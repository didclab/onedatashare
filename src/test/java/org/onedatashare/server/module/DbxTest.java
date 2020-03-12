/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.module;

import org.junit.Test;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.dropbox.DbxSession;

import java.net.URI;
import java.nio.file.Paths;

public class DbxTest {
//  @Test
//  public void lsTest() throws Exception {
//    OAuthCredential oAuthCredential = new OAuthCredential("AEvPwgNKPAAAAAAAAAAASb0cLnVGdy0IY5LzM2haSof0lqurWDJY0nz_MsxUF0Y2");
//    DbxSession dbxSession = new DbxSession(new URI(""), oAuthCredential);
//    DbxResource dbxResource = dbxSession.select(Paths.get("")).block();
//    dbxResource.list().doOnNext(System.out::println).subscribe();
//  }
//
//  @Test
//  public void statTest() throws Exception {
//    OAuthCredential oAuthCredential = new OAuthCredential("uunbJnWXSQwAAAAAAAAGKK9l_aT5AG7SPF06u66fD7gVuP8bC6wDDGSRFAhfTyB3");
//    DbxSession dbxSession = new DbxSession(new URI(""), oAuthCredential);
//    dbxSession.select(Paths.get("/Amazon Qeustions"))
//            .flatMap(DbxResource::stat)
//            .map(Stat::toString)
//            .subscribe(System.out::println);
//  }
//
//  @Test
//  public void tapTest() throws Exception {
////    OAuthCredential oAuthCredential = new OAuthCredential("AEvPwgNKPAAAAAAAAAAASb0cLnVGdy0IY5LzM2haSof0lqurWDJY0nz_MsxUF0Y2");
////    DbxSession dbxSession = new DbxSession(new URI("/1KB.zip"), oAuthCredential);
////    dbxSession.select(Paths.get("/1KB.zip"))
////            .flux()
////            .flatMapSequential(dbxResource -> dbxResource.tap(1024))
////            .subscribe(System.out::println);
//  }
//
//  @Test
//  public void mkdirTest() throws Exception {
//    OAuthCredential oAuthCredential = new OAuthCredential("AEvPwgNKPAAAAAAAAAAASb0cLnVGdy0IY5LzM2haSof0lqurWDJY0nz_MsxUF0Y2");
//    DbxSession dbxSession = new DbxSession(new URI(""), oAuthCredential);
//    dbxSession.select(Paths.get("/testing")).flatMap(DbxResource::mkdir).subscribe();
//  }
//
//  @Test
//  public void deleteTest() throws Exception {
//    OAuthCredential oAuthCredential = new OAuthCredential("AEvPwgNKPAAAAAAAAAAASb0cLnVGdy0IY5LzM2haSof0lqurWDJY0nz_MsxUF0Y2");
//    DbxSession dbxSession = new DbxSession(new URI(""), oAuthCredential);
//    dbxSession.select(Paths.get("/testing/1KB.zip")).flatMap(DbxResource::delete).subscribe();
//  }
//
//  @Test
//  public void sinkTest() throws Exception {
////    OAuthCredential oAuthCredential = new OAuthCredential("AEvPwgNKPAAAAAAAAAAASb0cLnVGdy0IY5LzM2haSof0lqurWDJY0nz_MsxUF0Y2");
////    DbxSession dbxSession = new DbxSession(new URI(""), oAuthCredential);
//////    dbxSession.select(Paths.get("/testing/1KB.zip")).flatMap(DbxResource::delete).subscribe();
////    dbxSession.select(Paths.get("/1KB.zip"))
////            .map(dbxResource -> dbxResource.tap(1024))
////            .subscribe(sliceFlux -> {
////              dbxSession.select(Paths.get("/testing/1KB.zip"))
////                      .map(dbxResource -> dbxResource.sink(sliceFlux).subscribe())
////                      .subscribe();
////            });
//  }
}
