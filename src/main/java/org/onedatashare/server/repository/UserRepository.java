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


package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface UserRepository extends MongoRepository<User, String> {

    @Query(value = "{'isAdmin': true}", fields = "{'email' : 1, 'firstName' : 1, 'lastName' : 1, 'lastActivity' : 1 }")
    List<User> findAllAdministrators(Pageable pageable);

    @Query(value = "{'isAdmin': false}", fields = "{'email' : 1, 'organization' : 1, 'firstName' : 1, 'lastName' : 1, 'validated' : 1, 'registerMoment' : 1, 'lastActivity' : 1}")
    List<User> findAllUsers(Pageable pageable);

    @Query(value = "{'isAdmin' : true}", fields = "{'email' : 1}")
    List<User> getAllAdminIds();


    @Query(value = "{'isAdmin' : false}", fields = "{'email' : 1}")
    List<User> getAllUserEmailIds();

//    Flux<User> findAllBy(Pageable pageable);

    @Query(value="{'isAdmin': true}", count = true)
    Long countAdministrators();

    @Query(value="{'isAdmin': false}", count = true)
    Long countUsers();

}
