package de.xturbo77.alexa.steam.storage;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schmidt
 */
public class SteamUserDynamoDbClient {

    private static final Logger LOG = LoggerFactory.getLogger(SteamUserDynamoDbClient.class);

    private final AmazonDynamoDBClient dynamoDBClient;

    public SteamUserDynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
        this.dynamoDBClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
    }

    public void saveItem(final SteamUser steamUser) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(steamUser);
        LOG.info("saved steam user: {}", steamUser);
    }

    public SteamUser loadItem(SteamUser steamUser) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        return mapper.load(steamUser);
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     *
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }
}
