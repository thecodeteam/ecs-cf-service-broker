package com.emc.ecs.servicebroker.model;

import java.util.Collections;
import java.util.List;

public class Constants {
    public static final String NAMESPACE = "namespace";
    public static final String BUCKET = "bucket";

    public static final String RETENTION = "retention";
    public static final String BASE_URL = "base-url";
    public static final String USE_SSL = "use-ssl";
    public static final String REPLICATION_GROUP = "replication-group";
    public static final String DEFAULT_RETENTION = "default-retention";
    public static final String PATH_STYLE_ACCESS = "path-style-access";
    public static final String SERVICE_TYPE = "service-type";
    public static final String CERTIFICATE = "certificate";
    public static final String DEFAULT_BUCKET_QUOTA = "default-bucket-quota";
    public static final String COMPLIANCE_ENABLED = "compliance-enabled";
    public static final String FILE_ACCESSIBLE = "file-accessible";
    public static final String ACCESS_DURING_OUTAGE = "access-during-outage";
    public static final String HEAD_TYPE = "head-type";
    public static final String ENDPOINT = "endpoint";
    public static final String ENCRYPTED = "encrypted";
    public static final String DOMAIN_GROUP_ADMINS = "domain-group-admins";

    public static final String RECLAIM_POLICY = "reclaim-policy";
    public static final String ALLOWED_RECLAIM_POLICIES = "allowed-reclaim-policies";

    public static final String QUOTA = "quota";
    public static final String QUOTA_WARN = "warn";
    public static final String QUOTA_LIMIT = "limit";
    public static final String S3_URL = "s3Url";

    public static final String TAGS = "tags";

    public static final String VOLUME_EXPORT = "export";
    public static final String VOLUME_EXPORT_SOURCE = "source";
    public static final String VOLUME_EXPORT_UID = "uid";
    public static final String VOLUME_MOUNT = "mount";
    public static final String VOLUME_DRIVER = "nfsv3driver";
    public static final String VOLUME_DEFAULT_MOUNT = "/var/vcap/data";

    public static final String REMOTE_CONNECTION = "remote_connection";

    public static final String CREDENTIALS_ACCESS_KEY = "accessKey";
    public static final String CREDENTIALS_SECRET_KEY = "secretKey";
    public static final String CREDENTIALS_INSTANCE_ID = "instanceId";

    public static final String USER_PERMISSIONS = "permissions";
    public static final List<String> FULL_CONTROL = Collections.singletonList("full_control");

    public static String NAME_PARAMETER = "name";

    public static final String HEAD_TYPE_S3 = "s3";
}
