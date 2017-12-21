package de.xturbo77.alexa.steam.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 *
 * @author schmidt
 */
@DynamoDBTable(tableName = "steam-user")
public class SteamUser {

    private String aznid;
    private String steamid;

    public SteamUser() {
    }

    public SteamUser(String aznid) {
        this.aznid = aznid;
    }

    @DynamoDBHashKey(attributeName = "aznid")
    public String getAmazonUserId() {
        return aznid;
    }

    public void setAmazonUserId(String aznid) {
        this.aznid = aznid;
    }

    @DynamoDBAttribute(attributeName = "steamid")
    public String getSteamId() {
        return steamid;
    }

    public void setSteamId(String steamid) {
        this.steamid = steamid;
    }
}
