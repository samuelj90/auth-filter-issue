insert into OAUTH_CLIENT_DETAILS (
    CLIENT_ID,
    CLIENT_SECRET,
    AUTHORIZED_GRANT_TYPES,
    AUTHORITIES,
    ACCESS_TOKEN_VALIDITY,
    REFRESH_TOKEN_VALIDITY,
    AUTOAPPROVE
) values (
    'clientId',
    '{noop}password',
    'client_credentials',
    'ROLE_UI',
    600,
    600,
    'true'
);