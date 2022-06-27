package org.onedatashare.server.service.management;

import com.influxdb.client.InfluxDBClient;
import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class InfluxDBManager implements Manager{

    @Autowired
    private InfluxDBClient influxClient;

    @Override
    public Mono<User> createUser(User user) {
        String bucketId = "";
        //com.influxdb.client.domain.User influxUser = this.client.getUsersApi().createUser(user.getEmail())
        //InfluxDBClientFactory.
                //onBoarding(INFLUXDB_URL, user.getEmail(), user.get, ORGANIZATION, user.getEmail()).getBucket().getId();
        if(bucketId.isEmpty() || bucketId == null){
            return Mono.just(new User());
        }
        return Mono.just(user);
    }

    @Override
    public Mono<Void> deleteUser(User user) {

        return null;
    }

    @Override
    public Mono<Void> setPermissions() {

        return null;
    }
}
