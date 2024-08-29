drop table if exists OAUTH_CLIENT_DETAILS;
create table OAUTH_CLIENT_DETAILS (
    CLIENT_ID varchar(256) not null,
    RESOURCE_IDS varchar(4000),
    CLIENT_SECRET varchar(256),
    SCOPE varchar(30),
    AUTHORIZED_GRANT_TYPES varchar(128),
    WEB_SERVER_REDIRECT_URI varchar(256),
    AUTHORITIES varchar(256),
    ACCESS_TOKEN_VALIDITY integer,
    REFRESH_TOKEN_VALIDITY integer,
    ADDITIONAL_INFORMATION varchar(2000),
    AUTOAPPROVE varchar(256),
    primary key (CLIENT_ID)
);